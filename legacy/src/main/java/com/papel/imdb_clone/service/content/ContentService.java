package com.papel.imdb_clone.service.content;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for content services (Movies, Series, etc.)
 * @param <T> The type of content this service manages
 */
public interface ContentService<T> {

    /**
     * Get all content items
     * @return List of all content items
     */
    List<T> getAll();
    
    /**
     * Get content by ID
     * @param id The ID of the content to retrieve
     * @return Optional containing the content if found, empty otherwise
     */
    Optional<T> getById(int id);
    
    /**
     * Save a new content item
     * @param content The content to save
     * @return The saved content with generated ID
     */
    T save(T content);
    
    /**
     * Update an existing content item
     * @param content The content to update
     * @return The updated content
     */
    T update(T content);
    
    /**
     * Delete a content item by ID
     * @param id The ID of the content to delete
     * @return true if the content was deleted, false otherwise
     */
    boolean delete(int id);

    /**
     * Removes a content item.
     * @param content The content to remove
     * @return true if the content was removed, false otherwise
     */
    boolean remove(T content);

    /**
     * Find content by title and year
     * @param title The title to search for
     * @param year The release year to search for
     * @return Optional containing the content if found, empty otherwise
     */
    Optional<T> findByTitleAndYear(String title, int year);
    
    /**
     * Get the content type this service manages
     * @return The content type as a string (e.g., "Movie", "Series")
     */
    String getContentType();

}
