package com.papel.imdb_clone.repository;

import com.papel.imdb_clone.model.people.Celebrity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Celebrity entity operations.
 * Provides abstraction for data access operations related to celebrities (actors and directors).
 */
public interface CelebritiesRepository {
    
    /**
     * Saves a celebrity to the repository.
     * @param celebrity The celebrity to save
     * @return The saved celebrity with generated ID if applicable
     */
    <T extends Celebrity> T save(T celebrity);
    
    /**
     * Finds a celebrity by ID.
     * @param id The ID of the celebrity to find
     * @param type The class type of the celebrity (e.g., Actor.class, Director.class)
     * @param <T> The type of celebrity
     * @return An Optional containing the found celebrity, or empty if not found
     */
    <T extends Celebrity> Optional<T> findById(int id, Class<T> type);
    
    /**
     * Finds all celebrities of a specific type.
     * @param type The class type of the celebrities to find
     * @param <T> The type of celebrity
     * @return A list of all celebrities of the specified type
     */
    <T extends Celebrity> List<T> findAll(Class<T> type);
    
    /**
     * Finds celebrities by name (case-insensitive partial match).
     * @param name The name or part of the name to search for
     * @param type The class type of the celebrities to find
     * @param <T> The type of celebrity
     * @return A list of matching celebrities of the specified type
     */
    <T extends Celebrity> List<T> findByNameContaining(String name, Class<T> type);
    
    /**
     * Finds a celebrity by their full name.
     * @param firstName The first name of the celebrity
     * @param lastName The last name of the celebrity
     * @param type The class type of the celebrity
     * @param <T> The type of celebrity
     * @return An Optional containing the found celebrity, or empty if not found
     */
    <T extends Celebrity> Optional<T> findByFullName(String firstName, String lastName, Class<T> type);
    
    /**
     * Deletes a celebrity by ID.
     * @param id The ID of the celebrity to delete
     * @return true if a celebrity was deleted, false otherwise
     */
    boolean deleteById(int id);
    
    /**
     * Checks if a celebrity with the given ID exists.
     * @param id The ID to check
     * @return true if a celebrity with the ID exists, false otherwise
     */
    boolean existsById(int id);
    
    /**
     * Returns the total count of celebrities of a specific type in the repository.
     * @param type The class type of the celebrities to count
     * @return The count of celebrities of the specified type
     */
    <T extends Celebrity> long count(Class<T> type);
}
