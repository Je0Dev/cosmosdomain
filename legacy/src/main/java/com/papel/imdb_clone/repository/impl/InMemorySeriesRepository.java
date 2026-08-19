package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.DuplicateEntryException;
import com.papel.imdb_clone.model.content.Series;
import com.papel.imdb_clone.repository.SeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory implementation of SeriesRepository.
 * Thread-safe implementation using CopyOnWriteArrayList and ReentrantReadWriteLock.
 */
public class InMemorySeriesRepository implements SeriesRepository {

    //Logger
    private static final Logger logger = LoggerFactory.getLogger(InMemorySeriesRepository.class);

    /**
     * Constructs a new InMemorySeriesRepository instance.
     */
    public InMemorySeriesRepository() {
        // Initialization if needed
        logger.info("InMemorySeriesRepository initialized");
    }

    private final List<Series> seriesList = new CopyOnWriteArrayList<>();
    //Thread-safe list of series which means that other threads cannot modify the list while this thread is reading from it
    private final AtomicInteger nextId = new AtomicInteger(1);
    //AtomicInteger is a thread-safe implementation of the Integer class which means that other threads cannot modify the value while this thread is reading from it
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    //ReentrantReadWriteLock is a thread-safe implementation of the ReadWriteLock interface which means that other threads cannot modify the list while this thread is reading from it

    /**
     * Finds a series by its ID.
     * @param id The series ID
     * @return Optional containing the series if found, empty otherwise
     */
    @Override
    public Optional<Series> findById(int id) {
        lock.readLock().lock();
        try {
            /*
             * Returns the first series that matches the given ID.
             */
            return seriesList.stream()
                    .filter(series -> series.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds a series by its title.
     * @param title The series title to search for
     * @return Optional containing the series if found, empty otherwise
     */
    @Override
    public Optional<Series> findByTitle(String title) {
        if (title == null) return Optional.empty();

        lock.readLock().lock();
        try {
            /*
             * Returns the first series that matches the given title.
             */
            return seriesList.stream()
                    .filter(series -> title.equals(series.getTitle()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns a list of all series.
     * @return List of all series
     */
    @Override
    public List<Series> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(seriesList);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves a series to the repository.
     * @param series The series to save
     * @return The saved series
     */
    @Override
    public Series save(Series series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        //Lock the write lock to prevent concurrent modification which means that other threads cannot modify the list while this thread is writing to it
        lock.writeLock().lock();
        try {
            /*
             * If the series ID is 0, it is a new series and we need to check for duplicate title.
             */
            if (series.getId() == 0) {
                // New series - check for duplicate title
                if (existsByTitle(series.getTitle())) {
                    throw new DuplicateEntryException("Series", series.getId(), "title", series.getTitle());
                }
                series.setId(nextId.getAndIncrement());
                seriesList.add(series);
                logger.debug("Created new series: {} with ID: {}", series.getTitle(), series.getId());
            } else {
                // Existing series - update
                Optional<Series> existing = findById(series.getId());
                if (existing.isPresent()) {
                    // Check if title is being changed to an existing one
                    if (!existing.get().getTitle().equals(series.getTitle()) &&
                            existsByTitle(series.getTitle())) {
                        throw new DuplicateEntryException("Series", series.getId(), "title", series.getTitle());
                    }
                    /*
                     * Remove the existing series and add the updated series.
                     */
                    seriesList.remove(existing.get());
                    seriesList.add(series);
                    logger.debug("Updated series: {} with ID: {}", series.getTitle(), series.getId());
                } else {
                    throw new IllegalArgumentException("Series with ID " + series.getId() + " not found");
                }
            }
            /*
             * Return the updated series.
             */
            return series;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Deletes a series by its ID.
     * @param id The ID of the series to delete
     * @return true if the series was deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        lock.writeLock().lock();
        try {
            /*
             * Find the series by ID.
             * If the series is found, remove it from the list.
             */
            Optional<Series> existing = findById(id);
            if (existing.isPresent()) {
                seriesList.remove(existing.get());
                logger.debug("Deleted series with ID: {}", id);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if a series with the given title exists.
     * @param title The title of the series to check
     * @return true if a series with the given title exists, false otherwise
     */
    @Override
    public boolean existsByTitle(String title) {
        if (title == null) return false;

        lock.readLock().lock();
        try {
            return seriesList.stream()
                    .anyMatch(series -> title.equals(series.getTitle()));
        } finally {
            lock.readLock().unlock();
        }
    }

    //Count total series size
    @Override
    public long count() {
        return seriesList.size();
    }

    //Delete series by title
    @Override
    public void deleteByTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        //Lock the write lock to ensure thread safety
        lock.writeLock().lock();
        try {
            //removeIf is a method that removes all elements from the list that match the given predicate which means that it will remove all series with the given title
            boolean removed = seriesList.removeIf(series -> title.equalsIgnoreCase(series.getTitle()));
            if (removed) {
                logger.debug("Deleted series with title: {}", title);
            } else {
                logger.warn("No series found with title: {}", title);
                throw new NoSuchElementException("Series with title " + title + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Delete all InMemory series
     */
    @Override
    public void deleteAll() {
        lock.writeLock().lock();
        try {
            seriesList.clear();
            logger.debug("All series have been deleted from the repository");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update InMemory series
     * @param series The series to update
     * @return The updated series
     */
    @Override
    public Series update(Series series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        //Lock the write lock to prevent concurrent modification which means that other threads cannot modify the list while this thread is writing to it
        lock.writeLock().lock();
        try {
            // Check if series exists
            Optional<Series> existing = findById(series.getId());
            if (existing.isPresent()) {
                // Check if title is being changed to an existing one
                if (!existing.get().getTitle().equals(series.getTitle()) && 
                    existsByTitle(series.getTitle())) {
                    throw new DuplicateEntryException("Series", series.getId(), "title", series.getTitle());
                }
                // Remove the existing series and add the updated one
                seriesList.remove(existing.get());
                seriesList.add(series);
                logger.debug("Updated series: {} with ID: {}", series.getTitle(), series.getId());
                return series;
            } else {
                throw new NoSuchElementException("Series with ID " + series.getId() + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update InMemory series rating
     * @param title The title of the series to update
     * @param rating The new rating
     */
    @Override
    public void updateRating(String title, double rating) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            //Find the series by title
            Optional<Series> seriesOptional = seriesList.stream()
                    .filter(s -> title.equalsIgnoreCase(s.getTitle()))
                    .findFirst();

            //If the series is found, update its rating
            if (seriesOptional.isPresent()) {
                Series series = seriesOptional.get();
                series.setRating(rating);
                logger.debug("Updated rating for series '{}' to {}", title, rating);
            } else {
                logger.warn("No series found with title: {}", title);
                throw new NoSuchElementException("Series with title " + title + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update InMemory series genre
     * @param title The title of the series to update
     * @param genre The new genre
     */
    @Override
    public void updateGenre(String title, String genre) {
        if (title == null || genre == null) {
            throw new IllegalArgumentException("Title and genre cannot be null");
        }
        //Lock the write lock to ensure thread safety
        lock.writeLock().lock();
        try {
            Optional<Series> seriesOptional = seriesList.stream()
                    .filter(s -> title.equalsIgnoreCase(s.getTitle()))
                    .findFirst();
                    
            if (seriesOptional.isPresent()) {
                Series series = seriesOptional.get();
                series.setGenre(Genre.valueOf(genre));
                logger.debug("Updated genre for series '{}' to '{}'", title, genre);
            } else {
                logger.warn("No series found with title: {}", title);
                throw new NoSuchElementException("Series with title " + title + " not found");
            }
        } finally {
            //Unlock the write lock
            lock.writeLock().unlock();
        }
    }
}
