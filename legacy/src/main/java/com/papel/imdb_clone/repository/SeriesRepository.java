package com.papel.imdb_clone.repository;

import com.papel.imdb_clone.model.content.Series;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Series entity operations.
 * Provides abstraction for data access operations.
 */
public interface SeriesRepository {
    /**
     * Finds a series by its unique ID.
     *
     * @param id The series ID
     * @return Optional containing the series if found, empty otherwise
     */
    Optional<Series> findById(int id);

    /**
     * Finds a series by its title.
     * @param title The title of the series to search for
     * @return An Optional containing the series if found, empty otherwise
     */
    Optional<Series> findByTitle(String title);

    /**
     * Returns all series in the repository.
     * @return A list of all series
     */
    List<Series> findAll();

    /**
     * Saves a series (create or update).
     *
     * @param series The series to save
     * @return The saved series with generated ID if new
     */
    Series save(Series series);

    /**
     * Deletes a series by its ID.
     *
     * @param id The ID of the series to delete
     * @return true if the series was found and deleted, false otherwise
     */
    boolean deleteById(int id);

    /**
     * Checks if a series with the given title exists.
     *
     * @param title The series title to check
     * @return true if exists, false otherwise
     */
    boolean existsByTitle(String title);

    /**
     * Gets the total count of series.
     *
     * @return The number of series
     */
    long count();

    /**
     * Deletes a series by its title.
     *
     * @param title The title of the series to delete
     */
    void deleteByTitle(String title);

    /**
     * Deletes all series from the repository.
     */
    void deleteAll();

    /**
     * Updates a series information.
     *
     * @param series The series to update
     * @return The updated series
     */
    Series update(Series series);

    /**
     * Updates a series rating.
     *
     * @param title The title of the series to update
     * @param rating The new rating
     */
    void updateRating(String title, double rating);


    /**
     * Updates a series's genre.
     *
     * @param title The title of the series to update
     * @param genre The new genre
     */
    void updateGenre(String title, String genre);

}
