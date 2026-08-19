-- V9__favorites_ratings.sql
-- Favorites, ratings, and local media tables

CREATE TABLE favorites (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    media_type      VARCHAR(20) NOT NULL,
    media_id        BIGINT NOT NULL,
    created_at      DATE NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT fk_favorite_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE ratings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    media_type      VARCHAR(20) NOT NULL,
    media_id        BIGINT NOT NULL,
    score           INTEGER NOT NULL,
    created_at      DATE NOT NULL DEFAULT CURRENT_DATE,
    updated_at      DATE NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT fk_rating_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
