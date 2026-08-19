-- V8__series_relationships.sql
-- Join tables for TV series relationships

-- Series genres
CREATE TABLE series_genres (
    series_id   BIGINT NOT NULL,
    genre_id    BIGINT NOT NULL,
    CONSTRAINT fk_series_genre_series
        FOREIGN KEY (series_id) REFERENCES tv_series(id) ON DELETE CASCADE,
    CONSTRAINT fk_series_genre_genre
        FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, genre_id)
);

-- Series actors (cast)
CREATE TABLE series_actors (
    series_id   BIGINT NOT NULL,
    person_id   BIGINT NOT NULL,
    character_name VARCHAR(255),
    sort_order  INTEGER,
    CONSTRAINT fk_series_actor_series
        FOREIGN KEY (series_id) REFERENCES tv_series(id) ON DELETE CASCADE,
    CONSTRAINT fk_series_actor_person
        FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, person_id)
);

-- Series creators (crew)
CREATE TABLE series_creators (
    series_id   BIGINT NOT NULL,
    person_id   BIGINT NOT NULL,
    CONSTRAINT fk_series_creator_series
        FOREIGN KEY (series_id) REFERENCES tv_series(id) ON DELETE CASCADE,
    CONSTRAINT fk_series_creator_person
        FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, person_id)
);
