package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.exceptions.AuthErrorType;
import com.papel.imdb_clone.exceptions.DataPersistenceException;
import com.papel.imdb_clone.exceptions.EntityNotFoundException;
import com.papel.imdb_clone.exceptions.InvalidInputException;
import com.papel.imdb_clone.model.people.Celebrity;
import com.papel.imdb_clone.repository.CelebritiesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Service for managing celebrities (actors, directors, etc.) in the system.
 * Provides CRUD operations and query capabilities for celebrity data.
 *
 * @param <T> The type of celebrity (e.g., Actor, Director)
 */
public class CelebrityService<T extends Celebrity> {

    private static final Logger logger = LoggerFactory.getLogger(CelebrityService.class);

    private final CelebritiesRepository celebritiesRepository;
    private final CelebrityManager<T> celebrityManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Class<T> celebrityType;


    public CelebrityService(Class<T> celebrityType, CelebritiesRepository celebritiesRepository) {
        this.celebrityType = celebrityType;
        this.celebritiesRepository = celebritiesRepository;
        this.celebrityManager = CelebrityManager.getInstance(celebrityType);
    }

    /**
     * Finds celebrities whose name contains the given search term (case-insensitive).
     *
     * @param name The name or part of the name to search for
     * @return List of matching celebrities, empty list if none found or if search term is empty
     * @throws InvalidInputException if the search term is null
     * @throws DataPersistenceException if there's an error accessing the data store
     */
    public List<T> findByName(String name) throws InvalidInputException {
        if (name == null) {
            throw new InvalidInputException("Search term cannot be null");
        }

        String searchTerm = name.trim();
        if (searchTerm.isEmpty()) {
            return List.of();
        }

        logger.debug("Searching for {} with name containing: '{}'", celebrityType.getSimpleName(), searchTerm);
        
        lock.readLock().lock();
        try {
            List<T> results = celebritiesRepository.findByNameContaining(searchTerm, celebrityType);
            logger.debug("Found {} {} matching '{}'", results.size(), 
                        celebrityType.getSimpleName().toLowerCase(), searchTerm);
            return results;
        } catch (Exception e) {
            String errorMsg = String.format("Error finding %s by name '%s'", 
                                          celebrityType.getSimpleName(), searchTerm);
            logger.error(errorMsg, e);
            throw new DataPersistenceException(
                errorMsg, 
                AuthErrorType.DATA_ACCESS_ERROR, 
                e
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * The minimum valid ID value for a celebrity which is the admin
     */
    private static final int MIN_ID = 1;
    
    /**
     * Validates a celebrity ID.
     *
     * @param id The ID to validate
     * @throws InvalidInputException if the ID is invalid
     */
    private void validateId(int id) throws InvalidInputException {
        if (id < MIN_ID) {
            throw new InvalidInputException(String.format("Invalid %s ID: %d (must be >= %d)", 
                celebrityType.getSimpleName().toLowerCase(), id, MIN_ID));
        }
    }
    
    /**
     * Finds a celebrity by their ID.
     *
     * @param id The ID of the celebrity to find
     * @return Optional containing the celebrity if found
     * @throws InvalidInputException if the ID is invalid
     * @throws DataPersistenceException if there's an error accessing the data store
     */
    public Optional<T> findById(int id) throws InvalidInputException {
        // Validate input
        validateId(id);
        logger.debug("Looking up {} with ID: {}", celebrityType.getSimpleName(), id);

        lock.readLock().lock();
        try {
            // First try to find in the repository
            Optional<T> celebrity = celebritiesRepository.findById(id, celebrityType);
            
            if (celebrity.isPresent()) {
                logger.debug("Found {} with ID {} in repository", celebrityType.getSimpleName(), id);
                return celebrity;
            }
            
            // If not found in repository, check with CelebrityManager
            logger.debug("{} with ID {} not found in repository, checking manager", 
                       celebrityType.getSimpleName(), id);
            
            try {
                Optional<T> celeb = celebrityManager.getCelebrityById(id);
                if (celeb.isPresent() && celebrityType.isInstance(celeb.get())) {
                    T managedCeleb = celebrityType.cast(celeb.get());
                    logger.debug("Found {} with ID {} in manager, saving to repository", 
                               celebrityType.getSimpleName(), id);
                    
                    // Save the celebrity from manager to repository
                    save(managedCeleb);
                    return Optional.of(managedCeleb);
                }
                
                logger.debug("No {} found with ID: {}", celebrityType.getSimpleName(), id);
                return Optional.empty();
                
            } catch (Exception e) {
                String errorMsg = String.format("Error retrieving %s with ID %d from manager", 
                                             celebrityType.getSimpleName(), id);
                logger.error(errorMsg, e);
                throw new DataPersistenceException(
                    errorMsg,
                    AuthErrorType.DATA_ACCESS_ERROR,
                    e
                );
            }
        } catch (Exception e) {
            String errorMsg = String.format("Error finding %s with ID %d", 
                                         celebrityType.getSimpleName(), id);
            logger.error(errorMsg, e);
            throw new DataPersistenceException(
                errorMsg,
                AuthErrorType.DATA_ACCESS_ERROR,
                e
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Validates celebrity data before saving.
     *
     * @param celebrity The celebrity to validate
     * @throws InvalidInputException if the celebrity data is invalid
     */
    private void validateCelebrity(T celebrity) throws InvalidInputException {
        if (celebrity == null) {
            throw new InvalidInputException("Celebrity cannot be null");
        }

        String firstName = celebrity.getFirstName();
        String lastName = celebrity.getLastName();
        
        // Handle null or empty names
        if (lastName == null || lastName.trim().isEmpty()) {
            if (firstName != null && !firstName.trim().isEmpty()) {
                // If last name is empty but first name has content, move first name to last name
                celebrity.setLastName(firstName.trim());
                celebrity.setFirstName("");
            } else {
                throw new InvalidInputException("Celebrity must have at least a last name");
            }
        } else {
            // Ensure last name is trimmed
            celebrity.setLastName(lastName.trim());
            
            // Trim first name if not null
            if (firstName != null) {
                celebrity.setFirstName(firstName.trim());
            } else {
                celebrity.setFirstName("");
            }
        }
    }
    
    /**
     * Saves a celebrity to the data store.
     *
     * @param celebrity The celebrity to save
     * @return The saved celebrity
     * @throws InvalidInputException if the celebrity data is invalid
     * @throws DataPersistenceException if there's an error saving the data
     */
    public T save(T celebrity) throws InvalidInputException {
        // Validate input
        validateCelebrity(celebrity);
        
        final String fullName = celebrity.getFullName();
        logger.debug("Saving {}: {}", celebrityType.getSimpleName(), fullName);

        lock.writeLock().lock();
        try {
            // Let CelebrityManager handle the ID assignment and duplicate checking
            try {
                celebrityManager.addCelebrity(celebrity);
            } catch (Exception e) {
                String errorMsg = String.format("Error adding %s to manager: %s %s", 
                                              celebrityType.getSimpleName(),
                                              celebrity.getFirstName(), 
                                              celebrity.getLastName());
                logger.error(errorMsg, e);
                throw new com.papel.imdb_clone.exceptions.DataPersistenceException(
                    errorMsg,
                    com.papel.imdb_clone.exceptions.AuthErrorType.DATA_ACCESS_ERROR,
                    e
                );
            }
            
            // Save to repository
            try {
                //Save the celebrity to the repository
                T savedCelebrity = celebritiesRepository.save(celebrity);
                
                if (savedCelebrity.getId() == celebrity.getId()) {
                    logger.info("Updated {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
                } else {
                    logger.info("Created new {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
                }
                
                return savedCelebrity;
            } catch (Exception e) {
                String errorMsg = String.format("Error saving %s: %s %s", 
                                              celebrityType.getSimpleName(),
                                              celebrity.getFirstName(), 
                                              celebrity.getLastName());
                logger.error(errorMsg, e);
                throw new com.papel.imdb_clone.exceptions.DataPersistenceException(
                    errorMsg,
                    com.papel.imdb_clone.exceptions.AuthErrorType.DATA_ACCESS_ERROR,
                    e
                );
            }
        } finally {
            //Release the lock
            lock.writeLock().unlock();
        }
    }

    /**
     * Validates that a name component is not null or blank.
     *
     * @param name The name to validate
     * @param fieldName The name of the field being validated (for error messages)
     * @throws InvalidInputException if the name is null or blank
     */
    private void validateName(String name, String fieldName) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException(String.format("%s cannot be null or empty", fieldName));
        }
    }
    
    /**
     * Finds a celebrity by their full name (first and last name).
     *
     * @param firstName The first name of the celebrity (can be empty but not null)
     * @param lastName  The last name of the celebrity (cannot be null or empty)
     * @return Optional containing the celebrity if found
     * @throws InvalidInputException if lastName is null or empty, or if firstName is null
     * @throws DataPersistenceException if there's an error accessing the data store
     */
    public Optional<T> findByFullName(String firstName, String lastName) throws InvalidInputException {
        // Validate inputs
        validateName(lastName, "Last name");
        if (firstName == null) {
            throw new InvalidInputException("First name cannot be null (can be empty)");
        }
        
        String trimmedFirstName = firstName.trim();
        String trimmedLastName = lastName.trim();
        
        logger.debug("Searching for {} with name: '{} {}'", 
                    celebrityType.getSimpleName(), trimmedFirstName, trimmedLastName);

        lock.readLock().lock();
        try {
            //Find the celebrity by full name
            Optional<T> result = celebritiesRepository.findByFullName(
                trimmedFirstName,
                trimmedLastName,
                celebrityType
            );

            //Log the result
            if (result.isPresent()) {
                logger.debug("Found {}: {} {}", celebrityType.getSimpleName(), 
                            trimmedFirstName, trimmedLastName);
            } else {
                logger.debug("No {} found with name: '{} {}'", 
                            celebrityType.getSimpleName(), trimmedFirstName, trimmedLastName);
            }
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = String.format("Error finding %s by name: '%s %s'", 
                                          celebrityType.getSimpleName(), 
                                          trimmedFirstName, 
                                          trimmedLastName);
            logger.error(errorMsg, e);
            throw new DataPersistenceException(
                errorMsg,
                AuthErrorType.DATA_ACCESS_ERROR,
                e
            );
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets all celebrities of this service's type.
     *
     * @return An unmodifiable list of all celebrities
     * @throws DataPersistenceException if there's an error accessing the data store
     */
    public List<T> getAll() {
        logger.debug("Retrieving all {} records", celebrityType.getSimpleName());
        
        lock.readLock().lock();
        try {
            List<T> celebrities = celebritiesRepository.findAll(celebrityType);
            logger.debug("Retrieved {} {} records", celebrities.size(), 
                       celebrityType.getSimpleName().toLowerCase());
            return List.copyOf(celebrities); // Return an unmodifiable copy
            
        } catch (Exception e) {
            String errorMsg = String.format("Error retrieving all %s records", 
                                          celebrityType.getSimpleName());
            logger.error(errorMsg, e);
            throw new DataPersistenceException(
                errorMsg,
                AuthErrorType.DATA_ACCESS_ERROR,
                e
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    
    /**
     * Returns the total count of celebrities of this service's type.
     * This method checks both the repository and the manager to ensure consistency.
     *
     * @return The total count of celebrities in the system
     * @throws DataPersistenceException if there's an error accessing the data store
     */
    public long count() {
        logger.debug("Counting total number of {}", celebrityType.getSimpleName().toLowerCase());
        
        lock.readLock().lock();
        try {
            long count = celebritiesRepository.count(celebrityType);
            logger.debug("Found {} {} in repository", count, 
                       count == 1 ? celebrityType.getSimpleName() : 
                       celebrityType.getSimpleName().toLowerCase() + "s");
            
            // For debugging purposes, log if there's a mismatch with the manager
            try {
                long managerCount = celebrityManager.getCelebrityCount();
                if (count != managerCount) {
                    logger.debug("Count mismatch - Repository: {}, Manager: {}", 
                               count, managerCount);
                }
            } catch (Exception e) {
                // Log but don't fail the operation
                logger.debug("Error getting count from manager: {}", e.getMessage());
            }
            
            return count;
            
        } catch (Exception e) {
            String errorMsg = String.format("Error counting %s records", 
                                          celebrityType.getSimpleName());
            logger.error(errorMsg, e);
            throw new DataPersistenceException(
                errorMsg,
                AuthErrorType.DATA_ACCESS_ERROR,
                e
            );
        } finally {
            //release read lock which means that other threads can read the list but cannot modify it
            lock.readLock().unlock();
        }
    }

    //delete celebrity by id
    public boolean delete(Integer id) {
        lock.writeLock().lock();
        try {
            return celebritiesRepository.deleteById(id);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
