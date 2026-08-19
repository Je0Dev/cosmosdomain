-- V6__episodes.sql
-- Episodes table for individual TV show episodes

CREATE TABLE episodes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    season_id       BIGINT NOT NULL,
    tmdb_id         BIGINT,
    episode_number  INTEGER,
    season_number   INTEGER,
    name            VARCHAR(255),
    overview        TEXT,
    air_date        DATE,
    still_path      VARCHAR(500),
    runtime         INTEGER,
    vote_average    DOUBLE PRECISION,
    youtube_trailer_key VARCHAR(50),
    CONSTRAINT fk_episode_season
        FOREIGN KEY (season_id) REFERENCES seasons(id) ON DELETE CASCADE
);
