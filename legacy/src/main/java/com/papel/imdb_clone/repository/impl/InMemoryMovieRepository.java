package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.DuplicateEntryException;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * In-memory implementation of MovieRepository.
 * Thread-safe implementation using CopyOnWriteArrayList and ReentrantReadWriteLock.
 */
public class InMemoryMovieRepository implements MovieRepository {

    //Logger
    private static final Logger logger = LoggerFactory.getLogger(InMemoryMovieRepository.class);

    /**
     * Constructs a new InMemoryMovieRepository instance.
     */
    public InMemoryMovieRepository() {
        // Initialization if needed
        logger.info("InMemoryMovieRepository initialized");
    }

    /**
     * List of movies stored in memory.
     * Thread-safe implementation using CopyOnWriteArrayList.
     */

    private static final List<Movie> movies = new CopyOnWriteArrayList<>();
    //Thread-safe implementation using CopyOnWriteArrayList. This means that the list is thread-safe and can be modified by multiple threads at the same time.
    private static final AtomicInteger nextId = new AtomicInteger(1);
    // AtomicInteger is used to generate unique IDs for movies. This means that every time a new movie is added, the ID is incremented by 1.
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    // ReentrantReadWriteLock is used to lock the list when it is being modified. This means that other threads cannot modify the list while this thread is modifying it.



    /**
     * Finds a movie by its ID.
     * @param id The movie ID
     * @return Optional containing the movie if found, empty otherwise
     */
    @Override
    public Optional<Movie> findById(int id) {
        lock.readLock().lock();
        try {
            //return the first movie that matches the id
            return movies.stream()
                    .filter(movie -> movie.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds movies by title.
     * @param title the title of the movie
     * @return List of movies that match the title
     */
    @Override
    public List<Movie> findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String searchTitle = title.trim().toLowerCase();
        //Lock the read lock to prevent concurrent modification which means that other threads cannot modify the list while this thread is reading it
        lock.readLock().lock();
        try {
            //return a list of movies that match the search title
            return movies.stream()
                    .filter(movie -> movie.getTitle().toLowerCase().contains(searchTitle))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves a movie to the repository.
     * @param movie The movie to save
     * @return The saved movie
     */
    @Override
    public Movie save(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        //Lock the write lock to prevent concurrent modification which means that other threads cannot modify the list while this thread is writing to it
        lock.writeLock().lock();
        try {
            if (movie.getId() == 0) {
                // New movie - check for duplicate title
                if (existsByTitle(movie.getTitle())) {
                    throw new DuplicateEntryException("Movie", movie.getId(), "title", movie.getTitle());
                }
                //Assign a unique ID to the new movie
                movie.setId(nextId.getAndIncrement());
                movies.add(movie);
                logger.debug("Created new movie: {} with ID: {}", movie.getTitle(), movie.getId());
            } else {
                // Update existing movie
                Optional<Movie> existing = findById(movie.getId());
                if (existing.isPresent()) {

                    // Check if title is being changed and if it conflicts
                    if (!existing.get().getTitle().equals(movie.getTitle()) &&
                            existsByTitle(movie.getTitle())) {
                        throw new DuplicateEntryException("Movie", movie.getId(), "title", movie.getTitle());
                    }

                    //Remove the existing movie and add the updated movie
                    movies.remove(existing.get());
                    movies.add(movie);
                    logger.debug("Updated movie: {} with ID: {}", movie.getTitle(), movie.getId());
                } else {
                    throw new IllegalArgumentException("Movie with ID " + movie.getId() + " not found");
                }
            }
            //Return the updated movie
            return movie;
        } finally {
            //Unlock the write lock when done,which means that other threads can modify the list
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if a movie with the given title exists in the repository.
     * @param title The movie title to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean existsByTitle(String title) {
        if (title == null) return false;

        lock.readLock().lock();
        try {
            /*
             * Check if any movie in the list has the same title as the given title.
             * Returns true if a movie with the given title exists, false otherwise.
             */
            return movies.stream()
                    .anyMatch(movie -> title.equals(movie.getTitle()));
        } finally {
            //Unlock the read lock when done,which means that other threads can read the list
            lock.readLock().unlock();
        }
    }

    //count the number of movies in the repository
    @Override
    public long count() {
        return movies.size();
    }

    /**
     * Finds a movie by its title and release year.
     * @param title the title of the movie
     * @param startYear the release year of the movie
     * @return the movie if found, null otherwise
     */
    @Override
    public Movie findByTitleAndReleaseYear(String title, int startYear) {
        lock.readLock().lock();
        try {
            /*
             * Find the first movie in the list that has the same title and release year as the given title and start year.
             * Returns the movie if found, null otherwise.
             */
            return movies.stream()
                    .filter(movie -> movie.getTitle().equalsIgnoreCase(title) &&
                            movie.getStartYear() == startYear)
                    .findFirst()
                    .orElse(null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            //Unlock the read lock when done,which means that other threads can read the list
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a movie to the repository.
     * @param movie the movie to add
     */
    @Override
    public void add(Movie movie) {
        addMovie(movie);
        logger.info("Added new movie: {} with ID: {}", movie.getTitle(), movie.getId());
    }

    /**
     * Updates a movie in the repository.
     * @param movie the movie to update
     */
    @Override
    public void update(Movie movie) {
        save(movie);
        logger.info("Updated movie: {} with ID: {}", movie.getTitle(), movie.getId());
    }

    /**
     * Delete an InMemory movie by id
     * @param id the id of the movie to delete
     */
    @Override
    public void deleteById(int id) {
        lock.writeLock().lock();
        try {
            Optional<Movie> movieOptional = findById(id);
            if (movieOptional.isPresent()) {
                movies.remove(movieOptional.get());
                logger.debug("Deleted movie with ID: {}", id);
            } else {
                logger.warn("Attempted to delete non-existent movie with ID: {}", id);
                throw new NoSuchElementException("Movie with ID " + id + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Delete an InMemory movie by title
     * @param title the title of the movie to delete
     */
    @Override
    public void deleteByTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            boolean removed = movies.removeIf(movie -> title.equalsIgnoreCase(movie.getTitle()));
            if (removed) {
                logger.debug("Deleted movie with title: {}", title);
            } else {
                logger.warn("Attempted to delete non-existent movie with title: {}", title);
                throw new NoSuchElementException("Movie with title " + title + " not found");
            }
        } finally {
            //Unlock the write lock when done,which means that other threads can modify the list
            lock.writeLock().unlock();
        }
    }

    /**
     * Delete all InMemory movies
     */
    @Override
    public void deleteAll() {
        lock.writeLock().lock();
        try {
            movies.clear();
            logger.debug("All movies have been deleted from the repository");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update Rating of a movie
     * @param title title of the movie
     * @param rating rating of the movie
     */
    @Override
    public void updateRating(String title, double rating) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        //Lock the write lock to ensure thread safety
        lock.writeLock().lock();
        try {
            Optional<Movie> movieOptional = movies.stream()
                    .filter(m -> title.equalsIgnoreCase(m.getTitle()))
                    .findFirst();
                    
            if (movieOptional.isPresent()) {
                Movie movie = movieOptional.get();
                movie.setRating(rating);
                logger.debug("Updated rating for movie '{}' to {}", title, rating);
            } else {
                logger.warn("No movie found with title: {}", title);
                throw new NoSuchElementException("Movie with title " + title + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update Genre of a movie
     * @param title title of the movie
     * @param genre genre of the movie
     */
    @Override
    public void updateGenre(String title, String genre) {
        if (title == null || genre == null) {
            throw new IllegalArgumentException("Title and genre cannot be null");
        }
        //Lock the write lock to ensure thread safety
        lock.writeLock().lock();
        try {
            Optional<Movie> movieOptional = movies.stream()
                    .filter(m -> title.equalsIgnoreCase(m.getTitle()))
                    .findFirst();
                    
            if (movieOptional.isPresent()) {
                Movie movie = movieOptional.get();
                movie.setGenre(Genre.valueOf(genre));
                logger.debug("Updated genre for movie '{}' to '{}'", title, genre);
            } else {
                logger.warn("No movie found with title: {}", title);
                throw new NoSuchElementException("Movie with title " + title + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update Release Year of a movie
     * @param title title of the movie
     * @param releaseYear release year of the movie
     */
    @Override
    public void updateReleaseYear(String title, int releaseYear) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        if (releaseYear < 1888) {  // First movie ever made was in 1888
            throw new IllegalArgumentException("Release year must be 1888 or later");
        }
        //Lock the write lock to ensure thread safety
        lock.writeLock().lock();
        try {
            Optional<Movie> movieOptional = movies.stream()
                    .filter(m -> title.equalsIgnoreCase(m.getTitle()))
                    .findFirst();
                    
            if (movieOptional.isPresent()) {
                Movie movie = movieOptional.get();
                movie.setStartYear(releaseYear);
                logger.debug("Updated release year for movie '{}' to {}", title, releaseYear);
            } else {
                logger.warn("No movie found with title: {}", title);
                throw new NoSuchElementException("Movie with title " + title + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Adds a movie directly to the repository (used by data loaders).
     *
     * @param movie The movie to add
     */
    public static void addMovie(Movie movie) {
        if (movie == null) return;

        lock.writeLock().lock();
        try {
            /*
             * If the movie has an ID, update the next ID to be the maximum of the current ID and the movie ID plus one
             */
            if (movie.getId() > 0) {
                nextId.getAndUpdate(current -> Math.max(current, movie.getId() + 1));
            } else {
                movie.setId(nextId.getAndIncrement());
            }
            movies.add(movie);
        } finally {
            /*
             * Unlock the write lock when done,which means that other threads can modify the list
             */
            lock.writeLock().unlock();
        }
    }

    //get all movies from the repository
    public List<Movie> getAll() {
        return movies;
    }

}