package com.example.postgresfulltextsearch.repository.entities;

import java.time.LocalDateTime;

public record UserStory(Long id, LocalDateTime createDate, Long numberOfViews,
                        String title, String body, Long userRating, String userName, Long userId) {
}
