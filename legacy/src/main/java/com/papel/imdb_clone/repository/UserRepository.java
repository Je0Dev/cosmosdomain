package com.papel.imdb_clone.repository;

import com.papel.imdb_clone.model.people.User;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides abstraction for data access operations.
 */
public interface UserRepository {
    /**
     * Finds a user by its unique ID.
     *
     * @param id The user ID
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findById(int id);

    /**
     * Finds a user by username.
     *
     * @param username The username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Saves a user (create or update).
     *
     * @param user The user to save
     * @return The saved user with generated ID if new
     */
    User save(User user);

    /**
     * Checks if a user with the given username exists.
     *
     * @param username The username to check
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Gets the total count of users.
     *
     * @return The number of users
     */
    long count();

    /**
     * Deletes a user by its unique ID.
     *
     * @param id The user ID
     */
    void deleteById(int id);

    /**
     * Deletes a user by its username.
     *
     * @param username The username of the user to delete
     */
    void deleteByUsername(String username);

    /**
     * Deletes all users from the repository.
     */
    void deleteAll();

    /**
     * Updates a user's information.
     *
     * @param user The user to update
     * @return The updated userS
     */
    User update(User user);

}
