-- V7__relationships.sql
-- Join tables for many-to-many relationships between content, people, and genres

-- Movie genres
CREATE TABLE movie_genres (
    movie_id    BIGINT NOT NULL,
    genre_id    BIGINT NOT NULL,
    CONSTRAINT fk_movie_genre_movie
        FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_movie_genre_genre
        FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

-- Movie actors (cast)
CREATE TABLE movie_actors (
    movie_id    BIGINT NOT NULL,
    person_id   BIGINT NOT NULL,
    character_name VARCHAR(255),
    sort_order  INTEGER,
    CONSTRAINT fk_movie_actor_movie
        FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_movie_actor_person
        FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

-- Movie directors (crew)
CREATE TABLE movie_directors (
    movie_id    BIGINT NOT NULL,
    person_id   BIGINT NOT NULL,
    CONSTRAINT fk_movie_director_movie
        FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_movie_director_person
        FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);
