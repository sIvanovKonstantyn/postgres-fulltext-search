--Create Users table
CREATE TABLE IF NOT EXISTS users
(
  id bigserial NOT NULL,
  name character varying(100) NOT NULL,
rating integer,
PRIMARY KEY (id)
)
;
CREATE INDEX usr_rating_idx
ON users USING btree
(rating ASC NULLS LAST)
TABLESPACE pg_default
;

--Create Stories table
CREATE TABLE  IF NOT EXISTS stories
(
    id bigserial NOT NULL,
    create_date timestamp without time zone NOT NULL,
    num_views bigint NOT NULL,
    title text NOT NULL,
    body text NOT NULL,
    fulltext tsvector,
    user_id bigint,
    PRIMARY KEY (id),
CONSTRAINT user_id_fk FOREIGN KEY (user_id)
REFERENCES users (id) MATCH SIMPLE
ON UPDATE NO ACTION
ON DELETE NO ACTION
NOT VALID
)
;
CREATE INDEX str_bt_idx
ON stories USING btree
(create_date ASC NULLS LAST,
num_views ASC NULLS LAST, user_id ASC NULLS LAST)
;

CREATE INDEX fulltext_search_idx
ON stories USING gin
(fulltext)
;
