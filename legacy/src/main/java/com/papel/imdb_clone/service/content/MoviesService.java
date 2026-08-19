package com.papel.imdb_clone.service.content;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.AuthErrorType;
import com.papel.imdb_clone.exceptions.DataPersistenceException;
import com.papel.imdb_clone.exceptions.InvalidInputException;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


/**
 * Service class for managing movies in the system.
 * Provides CRUD operations and query capabilities for movie data.
 * Implements thread-safe operations with proper exception handling.
 */
public class MoviesService extends BaseContentService<Movie> {

    private static final Logger logger = LoggerFactory.getLogger(MoviesService.class);
    private static volatile MoviesService instance;
    private static final Object instanceLock = new Object();

    private static final int MIN_YEAR = 1888; // First motion picture found in wikipedia =)
    private static final int MAX_YEAR = LocalDate.now().getYear() + 5; // Allow some future years
    private static final int MIN_DURATION = 1; // minutes
    private static final int MAX_DURATION = 1440; // 24 hours in minutes
    private static final double MIN_RATING = 0.0;//min rating
    private static final double MAX_RATING = 10.0;//max rating

    //constructor for singleton pattern which means only one instance of this class can be created
    private MoviesService() {
        super(Movie.class);
    }

    /**
     * Gets the singleton instance of MoviesService with double-checked locking.
     *
     * @return the singleton instance
     */
    public static MoviesService getInstance() {
        MoviesService result = instance;
        if (result == null) {
            synchronized (instanceLock) {
                result = instance;
                if (result == null) {
                    instance = result = new MoviesService();
                }
            }
        }
        return result;
    }

    /**
     * Loads movies from the movies_updated.txt file.
     * The expected format is: Title,Year,Genre,Duration,Director,Rating,Actors
     * Actors are separated by semicolons (;)
     */
    @Override
    protected void loadFromFile() {
        String filePath = "src/main/resources/data/content/movies_updated.txt";
        logger.debug("Loading movies from file: {}", filePath);
        //read lines from file
        List<String> lines;
        try {
            lines = FileUtils.readLines(filePath);
        } catch (Exception e) {
            String errorMsg = String.format("Failed to read movies file: %s", filePath);
            logger.error(errorMsg, e);
            throw new DataPersistenceException(errorMsg, AuthErrorType.INTERNAL_ERROR, e);
        }

        if (lines.isEmpty()) {
            logger.warn("No movies found in file: {}", filePath);
            return;
        }
        
        int successCount = 0;
        int errorCount = 0;

        //date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");

        //parse each line from the txt files
        for (String line : lines) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 6) {
                    logger.warn("Skipping invalid movie line (insufficient fields): {}", line);
                    errorCount++;
                    continue;
                }

                // Parse and validate fields
                String title = parts[0].trim();
                if (title.isEmpty()) {
                    logger.warn("Skipping movie with empty title: {}", line);
                    errorCount++;
                    continue;
                }

                //parse and validate year
                int year;
                try {
                    year = Integer.parseInt(parts[1].trim());
                    if (year < MIN_YEAR || year > MAX_YEAR) {
                        logger.warn("Skipping movie '{}' with invalid year: {}", title, year);
                        errorCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Skipping movie '{}' with invalid year format: {}", title, parts[1].trim());
                    errorCount++;
                    continue;
                }

                //parse and validate genre
                Genre genre;
                try {
                    genre = Genre.valueOf(parts[2].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Skipping movie '{}' with invalid genre: {}", title, parts[2].trim());
                    errorCount++;
                    continue;
                }

                //parse and validate duration
                int duration;
                try {
                    duration = Integer.parseInt(parts[3].trim());
                    if (duration < MIN_DURATION || duration > MAX_DURATION) {
                        logger.warn("Skipping movie '{}' with invalid duration: {} minutes", title, duration);
                        errorCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Skipping movie '{}' with invalid duration format: {}", title, parts[3].trim());
                    errorCount++;
                    continue;
                }
                
                String director = parts[4].trim();
                if (director.isEmpty()) {
                    director = "Unknown";
                }

                //parse and validate rating
                double rating;
                try {
                    rating = Double.parseDouble(parts[5].trim());
                    if (rating < MIN_RATING || rating > MAX_RATING) {
                        logger.warn("Movie '{}' has rating out of bounds ({}), will be capped", title, rating);
                        rating = Math.max(MIN_RATING, Math.min(rating, MAX_RATING));
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Movie '{}' has invalid rating format: {}, using default", title, parts[5].trim());
                    rating = 0.0;
                }

                // Create and save movie
                Movie movie = new Movie();
                try {
                    movie.setTitle(title);
                    movie.setYear(dateFormat.parse(String.valueOf(year)));
                    movie.setGenre(genre);
                    movie.setDuration(duration);
                    movie.setDirector(director);
                    movie.setImdbRating(rating);

                    // Add actors if available
                    if (parts.length > 6) {
                        String[] actorNames = parts[6].split(";");
                        for (String actorName : actorNames) {
                            String trimmedName = actorName.trim();
                            if (!trimmedName.isEmpty()) {
                                //split the name into first and last name
                                String[] nameParts = trimmedName.split("\\s+", 2);
                                Actor actor;
                                if (nameParts.length == 2) {
                                    //create actor instance
                                    actor = Actor.getInstance(nameParts[0], nameParts[1],
                                            LocalDate.now(), ' ', Ethnicity.UNKNOWN);
                                } else {
                                    // Handle single name
                                    actor = Actor.getInstance("", trimmedName,
                                            LocalDate.now(), ' ', Ethnicity.UNKNOWN);
                                }
                                //add actor to movie
                                movie.addActor(actor);
                            }
                        }
                    }
                    //save movie and add to list and increment success count
                    save(movie);
                    successCount++;
                } catch (ParseException e) {
                    logger.error("Failed to parse date for movie '{}': {}", title, e.getMessage());
                    errorCount++;
                } catch (Exception e) {
                    String errorMsg = String.format("Failed to save movie '%s'", title);
                    logger.error("{}: {}", errorMsg, e.getMessage(), e);
                    errorCount++;
                }
            } catch (Exception e) {
                logger.error("Unexpected error processing movie line: {}", line, e);
                errorCount++;
            }
        }

        logger.info("Completed loading movies. Success: {}, Failed: {}, Total: {}", 
                   successCount, errorCount, lines.size());

        //if there are errors, log them
        if (errorCount > 0) {
            logger.warn("Encountered {} errors while loading movies. Check logs for details.", errorCount);
        }
    }
    /**
     * Initializes sample data by loading from the default file.
     * This is called automatically during service initialization.
     */
    @Override
    protected void initializeSampleData() {
        lock.writeLock().lock();
        try {
            // Clear existing data
            contentList.clear();

            // Load movies from file
            loadFromFile();

            // If no movies were loaded from file, fall back to sample data
            if (contentList.isEmpty()) {
                logger.warn("No movies loaded from file, using sample data");

                // Sample data as fallback
                List<Genre> shawshankGenres = new ArrayList<>();
                shawshankGenres.add(Genre.DRAMA);
                List<Actor> shawshankActors = new ArrayList<>();
                //add actors
                shawshankActors.add(Actor.getInstance("Tim", "Robbins", LocalDate.of(1958, 10, 16), 'M', Ethnicity.CAUCASIAN));
                shawshankActors.add(Actor.getInstance("Morgan", "Freeman", LocalDate.of(1937, 6, 1), 'M', Ethnicity.AFRICAN));

                // Convert genres list to a comma-separated string
                String shawshankGenreStr = shawshankGenres.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

                Movie movie1 = new Movie(
                        "The Shawshank Redemption",
                        1994,
                        shawshankGenreStr,
                        "Frank Darabont",
                        new HashMap<>(),
                        9.3);
                movie1.setActors(shawshankActors);
                save(movie1);

                // Movie 2 - The Godfather
                List<Genre> godfatherGenres = new ArrayList<>();
                godfatherGenres.add(Genre.CRIME);
                godfatherGenres.add(Genre.DRAMA);
                List<Actor> godfatherActors = new ArrayList<>();
                //add actors
                godfatherActors.add(Actor.getInstance("Marlon", "Brando", LocalDate.of(1924, 4, 3), 'M', Ethnicity.CAUCASIAN));
                godfatherActors.add(Actor.getInstance("Al", "Pacino", LocalDate.of(1940, 4, 25), 'M', Ethnicity.CAUCASIAN));

                String godfatherGenreStr = godfatherGenres.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

                Movie movie2 = new Movie(
                        "The Godfather",
                        1972,
                        godfatherGenreStr,
                        "Francis Ford Coppola",
                        new HashMap<>(),
                        9.2
                );

                movie2.setActors(godfatherActors);
                save(movie2);

                // Movie 3 - The Dark Knight
                List<Genre> darkKnightGenres = new ArrayList<>();
                darkKnightGenres.add(Genre.ACTION);
                darkKnightGenres.add(Genre.CRIME);
                darkKnightGenres.add(Genre.DRAMA);
                List<Actor> darkKnightActors = new ArrayList<>();
                //add actors
                darkKnightActors.add(Actor.getInstance("Christian", "Bale", LocalDate.of(1974, 1, 30), 'M', Ethnicity.CAUCASIAN));
                darkKnightActors.add(Actor.getInstance("Heath", "Ledger", LocalDate.of(1979, 4, 4), 'M', Ethnicity.CAUCASIAN));

                //convert genres to string
                String darkKnightGenreStr = darkKnightGenres.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

                Movie movie3 = new Movie(
                        "The Dark Knight",
                        2008,
                        darkKnightGenreStr,
                        "Christopher Nolan",
                        new HashMap<>(),
                        9.0
                );
                movie3.setActors(darkKnightActors);
                save(movie3);

                logger.info("Initialized with {} sample movies", contentList.size());
            }
        } finally {
            //unlock the write lock when done, which means that other threads can modify the list
            lock.writeLock().unlock();
        }
        }
    }


