-- V3__genres.sql
-- Genres table for movies and TV series classification

CREATE TABLE genres (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_genre_id   INTEGER UNIQUE,
    name            VARCHAR(100) NOT NULL
);
