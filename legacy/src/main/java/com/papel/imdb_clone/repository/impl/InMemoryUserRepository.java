package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.exceptions.DuplicateEntryException;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory implementation of UserRepository.
 * Thread-safe implementation using CopyOnWriteArrayList and ReentrantReadWriteLock.
 */
public class InMemoryUserRepository implements UserRepository {


    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);

    /**
     * Constructs a new InMemoryUserRepository instance.
     */
    public InMemoryUserRepository() {
        // Initialize nextId by finding the maximum ID from the users list
        // This ensures we don't reuse IDs from the file
        int maxId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/data/people/users_updated.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // Skip comments and empty lines
                }
                //Split the line into parts
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException e) {
                        // Skip lines with invalid IDs
                        logger.warn("Skipping line with invalid ID: {}", line);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Could not read users_updated.txt, starting with ID 1", e);
        }
        this.nextId = new AtomicInteger(maxId + 1);
        logger.info("Initialized InMemoryUserRepository with nextId: {}", nextId.get());
    }

    //CopyOnWriteArrayList is a thread-safe implementation of List that uses a lock to ensure thread safety
    private final List<User> users = new CopyOnWriteArrayList<>();
    //AtomicInteger is a thread-safe implementation of Integer that uses a lock to ensure thread safety
    private final AtomicInteger nextId;
    //ReentrantReadWriteLock is a lock that allows multiple threads to read the list at the same time, but only one thread to write to the list at a time
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Finds a user by their ID.
     * @param id The user ID
     * @return The user with the given ID, or an empty Optional if not found
     */
    @Override
    public Optional<User> findById(int id) {
        lock.readLock().lock();
        try {
            return users.stream()
                    .filter(user -> user.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves a user to the repository.
     * @param user The user to save
     * @return The saved user
     */
    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        lock.writeLock().lock();
        try {
            if (user.getId() == 0) {
                // New user - check for duplicate username
                if (existsByUsername(user.getUsername())) {
                    throw new DuplicateEntryException("User", user.getId(), "username", user.getUsername());
                }
                user.setId(nextId.getAndIncrement());
                users.add(user);
                logger.debug("Created new user: {} with ID: {}", user.getUsername(), user.getId());
            } else {
                // Update existing user
                Optional<User> existing = findById(user.getId());
                if (existing.isPresent()) {
                    // Check if username is being changed and if it conflicts
                    if (!existing.get().getUsername().equals(user.getUsername()) &&
                            existsByUsername(user.getUsername())) {
                        throw new DuplicateEntryException("User", user.getId(), "username", user.getUsername());
                    }
                    users.remove(existing.get());
                    users.add(user);
                    logger.debug("Updated user: {} with ID: {}", user.getUsername(), user.getId());
                } else {
                    // User with this ID doesn't exist, but we have an ID
                    // This can happen if the user was created in another repository instance which means that the ID is already used
                    // or if the data was loaded from a file
                    if (existsByUsername(user.getUsername())) {
                        throw new DuplicateEntryException("User", user.getId(), "username", user.getUsername());
                    }
                    users.add(user);
                    // Update nextId to ensure we don't reuse this ID
                    nextId.getAndUpdate(current -> Math.max(current, user.getId() + 1));
                    logger.debug("Added existing user (ID: {}) as new user: {}", user.getId(), user.getUsername());
                }
            }
            return user;
        } finally {
            //Unlock the write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * Exists by username
     * @param username The username to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;

        lock.readLock().lock();
        try {
            //check if user with given username exists
            return users.stream()
                    .anyMatch(user -> username.equals(user.getUsername()));
        } finally {
            lock.readLock().unlock();
        }
    }

    //return user with given username
    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();

        lock.readLock().lock();
        try {
            //return user with given username
            return users.stream()
                    .filter(user -> username.equals(user.getUsername()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    //return number of users
    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return users.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteById(int id) {
    }

    @Override
    public void deleteByUsername(String username) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public User update(User user) {
        return null;
    }


    /**
     * Adds a user directly to the repository (used by data loaders).
     *
     * @param user The user to add
     */
    public void addUser(User user) {
        if (user == null) return;

        lock.writeLock().lock();
        try {
            //if user has id, update nextId which is used for generating new ids for new users
            if (user.getId() > 0) {
                nextId.getAndUpdate(current -> Math.max(current, user.getId() + 1));
            } else {
                user.setId(nextId.getAndIncrement());
            }
            users.add(user);
        } finally {
            //unlock the write lock when done,which means that other threads can modify the list
            lock.writeLock().unlock();
        }
    }
}