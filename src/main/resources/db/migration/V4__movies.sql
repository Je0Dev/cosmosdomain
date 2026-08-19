-- V4__movies.sql
-- Movies table for film catalog

CREATE TABLE movies (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id         BIGINT UNIQUE,
    title           VARCHAR(255) NOT NULL,
    overview        TEXT,
    release_date    DATE,
    poster_path     VARCHAR(500),
    backdrop_path   VARCHAR(500),
    vote_average    DOUBLE PRECISION,
    vote_count      INTEGER,
    runtime         INTEGER,
    tagline         TEXT,
    budget          BIGINT,
    revenue         BIGINT,
    status          VARCHAR(50),
    imdb_id         VARCHAR(20),
    home_page       TEXT,
    youtube_trailer_key VARCHAR(50)
);
