-- V10__local_media.sql
-- Local media (user's downloaded files) and subtitles for the integrated player

CREATE TABLE local_media (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    title               VARCHAR(255) NOT NULL,
    media_type          VARCHAR(20) NOT NULL,
    catalog_movie_id    BIGINT,
    catalog_series_id   BIGINT,
    catalog_episode_id  BIGINT,
    file_path           TEXT NOT NULL,
    file_size           BIGINT,
    duration_seconds    INTEGER,
    thumbnail_path      VARCHAR(500),
    added_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    playback_position   INTEGER DEFAULT 0,
    last_played         TIMESTAMP,
    CONSTRAINT fk_local_media_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE subtitles (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    local_media_id      BIGINT NOT NULL,
    language            VARCHAR(10),
    label               VARCHAR(100),
    file_path           TEXT NOT NULL,
    original_filename   VARCHAR(255),
    status              VARCHAR(20) DEFAULT 'UPLOADED',
    CONSTRAINT fk_subtitle_media
        FOREIGN KEY (local_media_id) REFERENCES local_media(id) ON DELETE CASCADE
);
