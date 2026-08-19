package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Content class is an abstract class that represents a content.
 * Abstract means it can't be instantiated directly, but can be used as a base class for other classes.
 */
public abstract class Content {

    private Date year; // Release year of the content
    private String director; // Director of the content
    private int id; // ID of the content
    public String title; // Title of the content
    protected Genre genre; // Genre of the content
    private Map<Integer, Integer> userRatings; // userId -> rating
    private Double imdbRating; // IMDb rating
    private Date releaseDate; // Release date
    private int startYear; // Start year


    private List<Genre> genres = new ArrayList<>();
    private List<Actor> actors = new ArrayList<>();


    private static final Logger logger = LoggerFactory.getLogger(Content.class);

    private Object contentType; // Content type
    
    /**
     * Gets the content type of this content.
     * @return The content type
     */
    public Object getContentType() {
        return contentType;
    }


    private String series;

    private List<String> awards = new ArrayList<>();

    private int endYear = 0; // 0 indicates ongoing series


    /**
     * Content constructor
     * @param title content title
     * @param year content release year
     * @param genre content genre
     * @param director content director
     * @param userRatings user ratings
     * @param imdbRating imdb rating
     */
    public Content(String title, Date year, Genre genre, String director, Map<Integer, Integer> userRatings, Double imdbRating) {
        this.title = title;
        this.genre = genre;
        this.director = director;
        this.userRatings = new HashMap<>();
        this.imdbRating = imdbRating;

        // Initialize release date and year
        if (year != null) {
            this.year = new Date(year.getTime());
            // Set the release date based on the year
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.year);
            //Set start year and release date
            this.startYear = cal.get(Calendar.YEAR);
            this.releaseDate = new Date(this.year.getTime());
        } else {
            // Default to current date if year is not provided
            this.year = new Date();
            Calendar cal = Calendar.getInstance();
            this.startYear = cal.get(Calendar.YEAR);
            this.releaseDate = new Date();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Genre getGenre() {
        return genre;
    }

    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }

    public String getDirector() {
        return director;
    }
    
    /**
     * Gets the end year of the content.
     * @return The end year if the content has ended, 0 if it's ongoing
     */
    public int getEndYear() {
        return endYear;
    }
    
    /**
     * Checks if the content is ongoing.
     * @return true if the content is ongoing (endYear is 0 or current year + 1), false otherwise
     */
    public boolean isOngoing() {
        if (endYear == 0) {
            return true;
        }
        // Also check if end year is current year + 1 (common placeholder for ongoing series)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return endYear >= currentYear;
    }
    
    /**
     * Sets the end year of the content.
     * @param endYear The end year of the content (0 for ongoing content)
     * @throws IllegalArgumentException if endYear is negative
     */
    public void setEndYear(int endYear) {
        if (endYear < 0) {
            throw new IllegalArgumentException("End year cannot be negative");
        }
        this.endYear = endYear;
    }
    
    /**
     * Sets the end year from an Object (for backward compatibility)
     * @param endYear The end year as an Object (will be converted to int)
     */
    public void setEndYear(Object endYear) {
        //Switch case to handle different types of end year
        switch (endYear) {
            //Handle null end year
            case null -> {
                logger.debug("End year is null, setting to 0 (ongoing)");
                this.endYear = 0;
            }
            //Handle Integer end year
            case Integer i -> {
                logger.debug("Setting end year from Integer: {}", endYear);
                setEndYear(i);
            }
            //Handle String end year
            case String s -> {
                String endYearStr = endYear.toString().trim();
                if (endYearStr.isEmpty() || endYearStr.equals("-") || endYearStr.equalsIgnoreCase("N/A")) {
                    logger.debug("Empty or invalid end year string '{}', setting to 0 (ongoing)", endYearStr);
                    this.endYear = 0;
                } else {
                    try {
                        int year = Integer.parseInt(endYearStr);
                        logger.debug("Parsed end year from string '{}' to: {}", endYearStr, year);
                        setEndYear(year);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid end year format: '{}'", endYearStr);
                        this.endYear = 0;
                    }
                }
            }
            //Handle default case
            default -> {
                logger.warn("Unexpected end year type: {}", endYear.getClass().getName());
                this.endYear = 0;
            }
        }
    }

    public Double getImdbRating() {
        return imdbRating;
    }

    public Date getYear() {
        return year;
    }

    public int getReleaseYear() {
        return startYear; // Using startYear as release year
    }

    public Map<Integer, Integer> getUserRatings() {
        return new HashMap<>(userRatings);
    }

    /**
     * Gets the start year of the content.
     *
     * @return the start year, or 0 if not set
     */
    public int getStartYear() {
        // If startYear is not set but releaseDate is, derive it from releaseDate
        if (startYear == 0 && releaseDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(releaseDate);
            startYear = cal.get(Calendar.YEAR);
        }
        return startYear;
    }

    /**
     * Gets the release date of the content
     *
     * @return the release date
     */
    public Date getReleaseDate() {
        return releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public List<Actor> getActors() {
        return actors != null ? List.copyOf(actors) : List.of(); // Updated to return an unmodifiable list
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
        if (genre != null && !genres.contains(genre)) {
            genres.add(genre);
        }
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setYear(Date year) {
        this.year = year;
    }


    /**
     * Adds an actor to this content's cast and updates the bidirectional relationship.
     * @param actor The actor to add (must not be null)
     */
    public void addActor(com.papel.imdb_clone.model.people.Actor actor) {
        if (actor == null) {
            return;
        }
        
        // Initialize actors list if needed
        if (this.actors == null) {
            this.actors = new ArrayList<>();
        }
        
        // Add actor to this content's cast if not already present
        if (!this.actors.contains(actor)) {
            this.actors.add(actor);
            
            // Update the actor's movies list if it's not already there
            try {
                if (this instanceof Movie && !actor.getMovies().contains(this)) {
                    actor.addMovie((Movie) this);
                }
            } catch (Exception e) {
                // Log the error but don't fail the entire operation
                System.err.println("Error updating actor's movie list: " + e.getMessage());
                logger.error("Error updating actor's movie list: {}", e.getMessage());
            }
        }
    }

    /**
     * Gets the year of the content as an integer
     * @return the year as an integer (e.g., 2023)
     */
    public int getYearAsInt() {
        if (this.year != null) {
            //Derive year from date
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.year);
            return cal.get(Calendar.YEAR);
        } else if (this.startYear > 0) {
            return this.startYear;
        } else if (this.releaseDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.releaseDate);
            return cal.get(Calendar.YEAR);
        }
        return 0; // Default if no date is set
    }

    /**
     * Sets the IMDb rating for this content
     *
     * @param imdbRating the IMDb rating to set (0.0 to 10.0)
     * @throws IllegalArgumentException if the rating is not between 0.0 and 10.0
     */
    public void setImdbRating(Double imdbRating) {
        if (imdbRating != null && (imdbRating < 0.0 || imdbRating > 10.0)) {
            throw new IllegalArgumentException("IMDb rating must be between 0.0 and 10.0");
        }
        this.imdbRating = imdbRating;
    }


    /**
     * Sets a user's rating for this content and updates the average rating
     * @param userId The ID of the user who is rating
     * @param rating The rating value (0-10)
     * @throws IllegalArgumentException if rating is not between 0 and 10
     */
    public void setUserRating(Integer userId, Integer rating)  throws IllegalArgumentException{
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 0 and 10");
        }

        try{
            //Derive user ratings from userRatings
            this.userRatings.put(userId, rating);
            //Derive imdbRating from userRatings which means update the average rating
            this.imdbRating = this.userRatings.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }catch (Exception e){
            logger.error("Error setting user rating", e);
            throw new IllegalArgumentException("Error setting user rating");
        }

        //Derive user ratings from userRatings
        if (userRatings == null) {
            userRatings = new HashMap<>();
        }
        
        // Store the user's rating
        userRatings.put(userId, rating);
        
        // Calculate new average rating
        double sum = userRatings.values().stream().mapToInt(Integer::intValue).sum();
        this.imdbRating = sum / userRatings.size();

    }

    /**
     * Adds a genre to the content's genres list
     *
     * @param genre the genre to add
     */
    public void addGenre(Genre genre) {
        if (genre != null && !genres.contains(genre)) {
            genres.add(genre);
            // If no primary genre is set, set it being the first genre
            if (this.genre == null) {
                this.genre = genre;
                this.genres.add(genre);
            }
        }
    }

    /**
     * Sets all genres for this content
     *
     * @param genres the list of genres to set
     */
    public void setGenres(List<Genre> genres) {
        this.genres = new ArrayList<>(genres);
        if (!genres.isEmpty()) {
            this.genre = genres.getFirst();
        } else {
            //Derive genre from genres
            this.genre = null;
            this.genres = null;
        }
    }

    /**
     * Sets the release date of the content and updates the start year accordingly.
     * @param releaseDate the release date to set
     */
    public void setReleaseDate(Date releaseDate) {

        //Derive release date from start year
        this.releaseDate = releaseDate != null ? new Date(releaseDate.getTime()) : null;

        // Update startYear based on the release date
        if (releaseDate != null) {
            //Derive start year from release date
            Calendar cal = Calendar.getInstance();
            cal.setTime(releaseDate);
            //Derive start year from release date
            this.startYear = cal.get(Calendar.YEAR);
        } else {
            //Derive release date from start year
            this.startYear = 0;
            this.year = null;
        }
    }

    /**
     * Sets the actors for this content
     *
     * @param actors the list of actors to set
     */
    public void setActors(List<Actor> actors) {
        //Derive actors from actors
        this.actors = actors != null ? new ArrayList<>(actors) : new ArrayList<>();
    }


    /**
     * Sets the start year of the content and updates the release date accordingly.
     * If the release date is not set or has a different year, it will be updated.
     *
     * @param startYear the start year to set (must be a 4-digit year)
     */
    public void setStartYear(int startYear) {
        this.startYear = startYear;

        // Update release date if it's not set or has a different year
        if (releaseDate == null || new Calendar.Builder().setInstant(releaseDate).build().get(Calendar.YEAR) != startYear) {
            try {
                //Derive release date from start year
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, startYear);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                this.releaseDate = cal.getTime();
            } catch (Exception e) {
                logger.error("Error setting start year: {}", e.getMessage());
            }
        }
    }

    public double getRating() {
        return imdbRating != null ? imdbRating : 0.0;
    }

    public CharSequence getCast() {
        return actors != null ? actors.toString() : "";
    }

    public String getSeries() {
        return series;
    }

    public List<String> getAwards() {
        return awards != null ? awards : new ArrayList<>();
    }

    
}