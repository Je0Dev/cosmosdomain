package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Celebrity;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages celebrity instances to prevent duplicates.
 * Implements the singleton pattern with type-safe generic support.
 * Uses generics to ensure type safety and avoid casting.
 * 
 * @param <T> The type of celebrity this manager handles (e.g., Actor, Director)
 */
public class CelebrityManager<T extends Celebrity> {

    private final Map<String, T> celebritiesByKey = new ConcurrentHashMap<>();//key is the name of the celebrity
    private final Map<Integer, T> celebritiesById = new ConcurrentHashMap<>();//id is the id of the celebrity
    private static final Map<Class<?>, CelebrityManager<?>> instances = new ConcurrentHashMap<>();//instances of the manager
    private final AtomicInteger nextId = new AtomicInteger(1);//next id for the celebrity

    protected CelebrityManager() {
        // Protected constructor for extension
    }

    /**
     * Returns the singleton instance of CelebrityManager for the specified type.
     * @param <T> The type of celebrity
     * @param clazz The class object of the celebrity type
     * @return The singleton instance for the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Celebrity> CelebrityManager<T> getInstance(Class<T> clazz) {
        return (CelebrityManager<T>) instances.computeIfAbsent(clazz, k -> new CelebrityManager<>());
    }
    
    /**
     * Returns the singleton instance of CelebrityManager for the base Celebrity type.
     * This method is provided for backward compatibility.
     * @param <T> The type of celebrity
     * @return The singleton instance for the base Celebrity type
     * Unchecked means that the compiler doesn't check the type safety of the code which can cause errors
     */
    @SuppressWarnings("unchecked")
    public static <T extends Celebrity> CelebrityManager<T> getInstance() {
        // Default to base Celebrity type if no specific type is provided
        return (CelebrityManager<T>) getInstance(Celebrity.class);
    }

    /**
     * Generates a unique key for a celebrity based on their name and birth date.
     * @param celebrity The celebrity to generate a key for
     * @return A unique string key
     */
    private String generateKey(Celebrity celebrity) {
        return String.format("%s|%s|%s",
            celebrity.getFirstName() != null ? celebrity.getFirstName().toLowerCase() : "",
            celebrity.getLastName() != null ? celebrity.getLastName().toLowerCase() : "",
            celebrity.getBirthDate() != null ? celebrity.getBirthDate().toString() : ""
        );
    }


    /**
     * Adds a celebrity to the manager if it doesn't already exist.
     * @param celebrity The celebrity to add (must not be null)
     * @return The celebrity that was added or the existing one if it already exists
     * @throws IllegalArgumentException if the celebrity is null
     */
    public synchronized T addCelebrity(T celebrity) {
        Objects.requireNonNull(celebrity, "Celebrity cannot be null");

        String key = generateKey(celebrity);
        
        // Check if a celebrity with the same key already exists
        T existing = celebritiesByKey.get(key);
        if (existing != null) {
            return existing;
        }
        
        // Assign a new ID if needed
        if (celebrity.getId() == 0) {
            int newId = nextId.getAndIncrement();
            celebrity.setId(newId);
        }
        
        // Add to both maps
        celebritiesByKey.put(key, celebrity);
        celebritiesById.put(celebrity.getId(), celebrity);
        return celebrity;
    }


    /**
     * Gets a celebrity by ID.
     * @param id The ID of the celebrity to retrieve
     * @return An Optional containing the celebrity if found, empty otherwise
     */
    public synchronized Optional<T> getCelebrityById(int id) {
        return Optional.ofNullable(celebritiesById.get(id));
    }

    /**
     * Clears all celebrities from the manager and resets the ID counter.
     */
    public synchronized void clear() {
        celebritiesByKey.clear();
        celebritiesById.clear();
        nextId.set(1);
    }
    
    /**
     * Gets the number of celebrities currently managed.
     * @return The number of celebrities
     */
    public synchronized int size() {
        return celebritiesById.size();
    }

    /**
     * Gets the number of unique celebrities currently managed.
     * @return The count of unique celebrities
     */
    public int getCelebrityCount() {
        return celebritiesByKey.size();
    }

    /**
     * Finds the celebrity by id
     * @param id id of the celebrity
     * @return Optional containing the celebrity if found, empty otherwise
     */
    public Optional<Celebrity> findById(int id) {
        return Optional.ofNullable(celebritiesById.get(id));
    }

    /**
     * Finds celebrity by key
     * @param celebrity celebrity to find
     * @return Optional containing the celebrity if found, empty otherwise
     */
    public Optional<T> findCelebrity(T celebrity) {
        return Optional.ofNullable(celebritiesByKey.get(generateKey(celebrity)));
    }
    
}
