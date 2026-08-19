-- V2__people.sql
-- People table for actors, directors, and cast/crew

CREATE TABLE people (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id         BIGINT UNIQUE,
    name            VARCHAR(255) NOT NULL,
    birth_date      DATE,
    biography       TEXT,
    profile_path    VARCHAR(500),
    known_for_department VARCHAR(100),
    place_of_birth  VARCHAR(255),
    gender          INTEGER
);

CREATE TABLE person_known_for (
    person_id       BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    CONSTRAINT fk_person_known_for
        FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE
);
