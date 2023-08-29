package com.example.postgresfulltextsearch.repository;

import com.example.postgresfulltextsearch.model.Search;
import com.example.postgresfulltextsearch.repository.entities.UserStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class UserStoryRepository {

    private final JdbcTemplate jdbcTemplate;


    @Autowired
    public UserStoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserStory> findByFilters(Search search) {
        return jdbcTemplate.query(
                """
                            SELECT
                             s.id id,
                             create_date, 
                             num_views, 
                             title, 
                             body, 
                             user_id, 
                             name user_name, 
                             rating user_rating 
                            FROM stories s INNER JOIN users u 
                            ON s.user_id = u.id
                            WHERE true
                        """ + buildDynamicFiltersText(search)
                        + " order by create_date desc offset ? limit ?",
                (rs, rowNum) -> new UserStory(
                        rs.getLong("id"),
                        rs.getTimestamp("create_date").toLocalDateTime(),
                        rs.getLong("num_views"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getLong("user_rating"),
                        rs.getString("user_name"),
                        rs.getLong("user_id")
                ),
                buildDynamicFilters(search)
        );
    }

    public void save(UserStory userStory) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(
                            """
                                        INSERT INTO stories (create_date, num_views, title, body, user_id)
                                            VALUES (?, ?, ?, ?, ?)
                                    """,
                            Statement.RETURN_GENERATED_KEYS
                    );
            ps.setTimestamp(1, Timestamp.valueOf(userStory.createDate()));
            ps.setLong(2, userStory.numberOfViews());
            ps.setString(3, userStory.title());
            ps.setString(4, userStory.body());
            ps.setLong(5, userStory.userId());

            return ps;
        }, keyHolder);

        Long generatedId = (Long) keyHolder.getKeys().get("id");

        if (generatedId != null) {
            updateFullTextField(generatedId);
        }
    }

    private void updateFullTextField(Long generatedId) {
        jdbcTemplate.update(
                """
                          UPDATE stories SET fulltext = to_tsvector(title || ' ' || body)
                            where id = ?
                        """,
                generatedId
        );
    }

    private Object[] buildDynamicFilters(Search search) {
        Stream<Object> filtersStream = search.ranges().stream()
                .flatMap(
                        range -> Stream.of((Object) range.min(), range.max())
                );

        Stream<Object> periodsStream = search.periods().stream()
                .flatMap(
                        range -> Stream.of((Object) Timestamp.valueOf(range.min()), Timestamp.valueOf(range.max()))
                );

        filtersStream = Stream.concat(filtersStream, periodsStream);

        if (!search.fullText().isBlank()) {
            filtersStream = Stream.concat(filtersStream, Stream.of(search.fullText()));
        }

        filtersStream = Stream.concat(filtersStream, Stream.of(search.offset(), search.limit()));

        return filtersStream.toArray();
    }

    private String buildDynamicFiltersText(Search search) {
        String rangesFilterString =
                Stream.concat(
                                search.ranges()
                                        .stream()
                                        .map(
                                                range -> String.format(" and %s between ? and ? ", range.fieldName())
                                        ),
                                search.periods()
                                        .stream()
                                        .map(
                                                range -> String.format(" and %s between ? and ? ", range.fieldName())
                                        )
                        )
                        .collect(Collectors.joining(" "));

        return rangesFilterString + buildFulltextFilterText(search.fullText());
    }

    private String buildFulltextFilterText(String fullText) {
        return fullText.isBlank() ? "" : " and fulltext @@ plainto_tsquery(?) ";
    }
}
