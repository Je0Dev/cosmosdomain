-- V5__tv_series.sql
-- TV series table for show catalog

CREATE TABLE tv_series (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id         BIGINT UNIQUE,
    name            VARCHAR(255) NOT NULL,
    overview        TEXT,
    first_air_date  DATE,
    last_air_date   DATE,
    poster_path     VARCHAR(500),
    backdrop_path   VARCHAR(500),
    vote_average    DOUBLE PRECISION,
    vote_count      INTEGER,
    number_of_seasons INTEGER,
    number_of_episodes INTEGER,
    status          VARCHAR(50),
    in_production   BOOLEAN DEFAULT FALSE,
    youtube_trailer_key VARCHAR(50)
);

CREATE TABLE seasons (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    series_id       BIGINT NOT NULL,
    tmdb_id         BIGINT,
    season_number   INTEGER,
    name            VARCHAR(255),
    overview        TEXT,
    air_date        DATE,
    poster_path     VARCHAR(500),
    episode_count   INTEGER,
    CONSTRAINT fk_season_series
        FOREIGN KEY (series_id) REFERENCES tv_series(id) ON DELETE CASCADE
);
