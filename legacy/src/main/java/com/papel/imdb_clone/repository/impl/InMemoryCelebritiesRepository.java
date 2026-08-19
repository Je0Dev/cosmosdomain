package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.model.people.Celebrity;
import com.papel.imdb_clone.repository.CelebritiesRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * Creates a new instance of InMemoryCelebritiesRepository.
 */
public class InMemoryCelebritiesRepository implements CelebritiesRepository {

    private final Map<Integer, Celebrity> celebrities = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private final Object lock = new Object();

    /**
     * Constructs a new InMemoryCelebritiesRepository.
     */
    public InMemoryCelebritiesRepository() {
        // Initialize the repository
        //We have this constructor because we need to initialize the repository before we load the data
    }

    /**
     * Saves a celebrity to the repository.
     * @param celebrity The celebrity to save
     * @return The saved celebrity with generated ID if applicable
     * @param <T> The type of celebrity
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> T save(T celebrity) {
        synchronized (lock) {
            if (celebrity.getId() == 0) {
                celebrity.setId(idGenerator.getAndIncrement());
            }
            celebrities.put(celebrity.getId(), celebrity);
            return celebrity;
        }
    }

    /**
     * Finds a celebrity by ID.
     * @param id The ID of the celebrity to find
     * @param type The class type of the celebrity (e.g., Actor.class, Director.class)
     * @return An Optional containing the found celebrity, or empty if not found
     * @param <T> The type of celebrity
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> Optional<T> findById(int id, Class<T> type) {
        Celebrity celebrity = celebrities.get(id);
        if (celebrity != null && type.isInstance(celebrity)) {
            return Optional.of((T) celebrity);
        }
        return Optional.empty();
    }

    /**
     * Finds all celebrities of a specific type.
     * @param type The class type of the celebrities to find
     * @return A list of all celebrities of the specified type
     * @param <T> The type of celebrity
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> List<T> findAll(Class<T> type) {
        return celebrities.values().stream()
                .filter(type::isInstance)
                .map(celebrity -> (T) celebrity)
                .collect(Collectors.toList());
    }

    /**
     * Finds celebrities by name (case-insensitive partial match).
     * @param name The name or part of the name to search for
     * @param type The class type of the celebrities to find
     * @return A list of matching celebrities of the specified type
     * @param <T> The type of celebrity
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> List<T> findByNameContaining(String name, Class<T> type) {
        String lowerCaseName = name.toLowerCase();
        return celebrities.values().stream()
                .filter(type::isInstance)
                .filter(celebrity -> celebrity.getFullName().toLowerCase().contains(lowerCaseName))
                .map(celebrity -> (T) celebrity)
                .collect(Collectors.toList());
    }

    /**
     * Finds a celebrity by full name (case-insensitive).
     * @param firstName The first name of the celebrity
     * @param lastName The last name of the celebrity
     * @param type The class type of the celebrity
     * @return An Optional containing the found celebrity, or empty if not found
     * @param <T> The type of celebrity
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> Optional<T> findByFullName(String firstName, String lastName, Class<T> type) {
        String searchFirstName = firstName != null ? firstName.trim().toLowerCase() : "";
        String searchLastName = lastName != null ? lastName.trim().toLowerCase() : "";
        
        return (Optional<T>) celebrities.values().stream()
                .filter(type::isInstance)
                .filter(celebrity -> {
                    String celebFirstName = celebrity.getFirstName() != null ? 
                            celebrity.getFirstName().toLowerCase() : "";
                    String celebLastName = celebrity.getLastName() != null ? 
                            celebrity.getLastName().toLowerCase() : "";
                    return celebFirstName.equals(searchFirstName) && 
                           celebLastName.equals(searchLastName);
                })
                .findFirst();
    }

    /**
     * Deletes a celebrity by ID.
     * @param id The ID of the celebrity to delete
     * @return true if the celebrity was deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        synchronized (lock) {
            return celebrities.remove(id) != null;
        }
    }

    /**
     * Checks if a celebrity with the given ID exists.
     * @param id The ID to check
     * @return true if a celebrity with the ID exists, false otherwise
     */
    @Override
    public boolean existsById(int id) {
        return celebrities.containsKey(id);
    }

    /**
     * Counts the number of celebrities of a specific type.
     * @param type The class type of the celebrities to count
     * @return The count of celebrities of the specified type
     * @param <T> The type of celebrity
     */
    @Override
    public <T extends Celebrity> long count(Class<T> type) {
        return celebrities.values().stream()
                .filter(type::isInstance)
                .count();
    }

    /**
     * Clears all celebrities from the repository.
     * Primarily used for testing purposes.
     */
    public void clear() {
        //We have this method because we need to clear the repository before we load the data.
        //Synchronized is used to prevent concurrent modification exceptions which means that
        //only one thread can modify the repository at a time.
        synchronized (lock) {
            celebrities.clear();
            idGenerator.set(1);
        }
    }
}
