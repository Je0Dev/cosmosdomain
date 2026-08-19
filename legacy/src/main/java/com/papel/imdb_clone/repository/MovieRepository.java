package com.papel.imdb_clone.repository;

import com.papel.imdb_clone.model.content.Movie;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Movie entity operations.
 * Provides abstraction for data access operations.
 */
public interface MovieRepository {

    /**
     * Finds a movie by its unique ID.
     *
     * @param id The movie ID
     * @return Optional containing the movie if found, empty otherwise
     */
    Optional<Movie> findById(int id);

    List<Movie> findByTitle(String title);

    /**
     * Saves a movie (create or update).
     *
     * @param movie The movie to save
     * @return The saved movie with generated ID if new
     */
    Movie save(Movie movie);

    /**
     * Checks if a movie with the given title exists.
     *
     * @param title The movie title to check
     * @return true if exists, false otherwise
     */
    boolean existsByTitle(String title);

    /**
     * Gets the total count of movies.
     *
     * @return The number of movies
     */
    long count();

    /**
     * Adds a movie to the repository.
     * @param movie The movie to add
     */
    void add(Movie movie);

    /**
     * Updates a movie in the repository.
     * @param movie The movie to update
     */
    void update(Movie movie);

    //implemented by not used.Kept for future use maybe
    void deleteById(int id);

    void deleteByTitle(String title);

    void deleteAll();

    void updateRating(String title, double rating);

    void updateGenre(String title, String genre);

    void updateReleaseYear(String title, int releaseYear);


    /**
     * Finds a movie by its title and release year.
     * @param title The title of the movie to search for
     * @param releaseYear The release year of the movie
     * @return The movie if found, null otherwise
     */
    Movie findByTitleAndReleaseYear(String title, int releaseYear);
}