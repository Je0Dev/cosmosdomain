package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a series of episodes.
 */
public class Series extends Content {

    // Year is managed by the parent class's startYear field
    private List<Season> seasons;
    private String director;  // Changed from Director to String to match parent class
    private List<Actor> actors;
    private List<String> awards;
    private double rating;
    private List<Genre> genres = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Series.class);
    // endYear is managed in the parent Content class
    private int seasonsCount;


    // Removed duplicate setEndYear method

    @Override
    public String getDirector() {
        return director;
    }
    
    /**
     * Sets the director of the series.
     * @param director The director's name as a String
     */
    public void setDirector(String director) {
        this.director = director;
    }

    /**
     * Creates a new Series with the given title and summary.
     *
     * @param title The title of the series
     */
    public Series(String title) {
        // Call parent constructor with minimal defaults
        super(title, null, null, null, null, 0.0);
        // Initialize fields directly to avoid "this" escape
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        this.genres = new ArrayList<>();
        
        // Set the current year using the parent class's setter method
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        setStartYear(cal.get(Calendar.YEAR));
    }

    /**
     * Creates a new Series with the given title, summary, genre, imdbRating, userRating, and startYear.
     * @param title The title of the series
     * @param genre The genre of the series
     * @param imdbRating The IMDB rating of the series
     * @param userRating The user rating of the series
     * @param startYear The start year of the series
     */
    public Series(String title, Genre genre, double imdbRating, double userRating, int startYear) {
        super(title, new java.util.Date(), genre, "", new HashMap<>(), imdbRating);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        this.rating = userRating;
        // Set start year using the parent class's setter method
        setStartYear(startYear);
    }

    public List<Season> getSeasons() {
        if (seasons == null) {
            seasons = new ArrayList<>();
        }
        return seasons;
    }

    public int getTotalSeasons() {
        return seasons != null ? seasons.size() : 0;
    }

    /**
     * Adds a new season to the series.
     * @param seasonNumber The season number
     * @param title The title of the season
     * @return The created Season object
     */
    public Season addSeason(int seasonNumber, String title) {
        if (seasons == null) {
            seasons = new ArrayList<>();
        }
        
        // Check if season number already exists
        if (seasons.stream().anyMatch(s -> s.getSeasonNumber() == seasonNumber)) {
            throw new IllegalArgumentException("Season " + seasonNumber + " already exists");
        }
        
        Season season = new Season(seasonNumber, this);
        season.setTitle(title);
        season.setYear(this.getStartYear());
        seasons.add(season);
        return season;
    }

    public int getTotalEpisodes() {
        logger.debug("Calculating total episodes for series: {}", getTitle());
        if (seasons == null || seasons.isEmpty()) {
            logger.debug("No seasons found for series: {}", getTitle());
            return 0;
        }
        
        int total = 0;
        logger.debug("Processing {} seasons for series: {}", seasons.size(), getTitle());
        
        for (int i = 0; i < seasons.size(); i++) {
            Season season = seasons.get(i);
            if (season == null) {
                logger.warn("Season at index {} is null for series: {}", i, getTitle());
                continue;
            }
            
            try {
                // Get episodes using the interface method
                List<?> episodes = season.getEpisodes();
                
                if (episodes == null) {
                    logger.warn("Episodes list is null for season {} of series: {}", 
                        season.getSeasonNumber(), getTitle());
                    continue;
                }

                // Verify all items in the list are Episodes
                for (Object episode : episodes) {
                    if (!(episode instanceof Episode)) {
                        logger.warn("Non-Episode object found in episodes list for season {} of series: {}", 
                            season.getSeasonNumber(), getTitle());
                        continue;
                    }
                }

                // Derive total episodes from season
                logger.debug("Season {} has {} episodes", season.getSeasonNumber(), episodes.size());
                total += episodes.size();
                
            } catch (Exception e) {
                logger.warn("Error getting episodes for season {} of series '{}': {}", 
                    season.getSeasonNumber(), getTitle(), e.getMessage(), e);
            }
        }
        
        logger.info("Total episodes for series '{}': {}", getTitle(), total);
        return total;
    }

    public void addSeason(Season season) {
        if (season != null) {
            season.setSeries(this);
            this.seasons.add(season);
        }
    }

    //getters and setters
    // Using parent class's getStartYear() implementation

    /**
     * Sets the list of actors in the series
     *
     * @param actors the list of actors to set
     */
    public void setActors(List<Actor> actors) {
        this.actors = new ArrayList<>(actors);
    }

    @Override
    public void setStartYear(int startYear) {
        // Call parent's setStartYear first
        super.setStartYear(startYear);
        
        // Get the current end year using the getter
        int currentEndYear = getEndYear();
        
        // If end year is set (not 0) and is before start year, update it
        if (currentEndYear != 0 && currentEndYear < startYear) {
            logger.info("Updating end year from {} to {} to match new start year for {}", 
                currentEndYear, startYear, getTitle());
            setEndYear(startYear);
        }
    }

    /**
     * Gets the end year of the series.
     * @return The end year if the series has ended, null if it's ongoing
     */
    @Override
    public int getEndYear() {
        return super.getEndYear();
    }

    //setEndYear
    @Override
    public void setEndYear(int endYear) {
        logger.debug("Setting end year for series '{}' to: {}", getTitle(), endYear);
        if (endYear != 0 && endYear < getStartYear()) {
            logger.warn("Attempted to set end year {} before start year {} for series '{}'", 
                endYear, getStartYear(), getTitle());
            throw new IllegalArgumentException("End year cannot be before start year");
        }
        // Call parent's setEndYear directly to avoid recursion
        super.setEndYear(endYear);
        logger.debug("Set end year to {} for series '{}'", endYear, getTitle());
    }
    
    // Keep the Object version for backward compatibility
    @Override
    public void setEndYear(Object endYear) {
        if (endYear == null) {
            logger.debug("End year is null, setting to 0 (ongoing)");
            super.setEndYear(0);  // Call parent's method directly
            return;
        }
        
        int yearValue;
        
        if (endYear instanceof Integer) {
            yearValue = (Integer) endYear;
        } else if (endYear instanceof String) {
            String endYearStr = endYear.toString().trim();
            if (endYearStr.isEmpty() || endYearStr.equals("-") || endYearStr.equalsIgnoreCase("N/A")) {
                logger.debug("Empty or invalid end year string '{}', setting to 0 (ongoing)", endYearStr);
                super.setEndYear(0);
                return;
            }
            try {
                yearValue = Integer.parseInt(endYearStr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid end year format: '{}'", endYearStr);
                super.setEndYear(0);
                return;
            }
        } else {
            logger.warn("Unexpected end year type: {}", endYear.getClass().getName());
            super.setEndYear(0);
            return;
        }
        
        // Now set the year value using the int version
        this.setEndYear(yearValue);
    }

    /**
     * Sets the number of seasons for this series.
     * Initializes empty seasons up to the specified count.
     * @param seasonsCount The number of seasons to create
     */
    public void setSeasons(int seasonsCount) {
        this.seasons = new ArrayList<>();
        for (int i = 0; i < seasonsCount; i++) {
            this.addSeason(new Season(i + 1, this));  // Pass 'this' (the Series) to the Season constructor
        }
    }

    public List<String> getAwards() {
        return new ArrayList<>(awards);
    }

    /**
     * Sets the list of genres for this series.
     *
     * @param genres the list of genres to set
     */
    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();

        // Set the primary genre to the first one if available
        if (!this.genres.isEmpty()) {
            setGenre(this.genres.getFirst());
        }
    }

    public void setSeasons(List<Season> seasons) {
        this.seasons = new ArrayList<>(seasons);
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    /**
     * Gets all actors from the main cast and all episodes.
     *
     * @return List of all unique actors in the series
     */
    public List<Actor> getActors() {
        Set<Actor> uniqueActors = new LinkedHashSet<>();
        
        // Add main cast first
        if (this.actors != null && !this.actors.isEmpty()) {
            uniqueActors.addAll(this.actors);
        }
        
        // Add actors from all episodes
        if (seasons != null) {
            for (Season season : seasons) {
                if (season != null && season.getEpisodes() != null) {
                    for (Object episodeObj : season.getEpisodes()) {
                        if (episodeObj instanceof Episode episode) {
                            if (episode.getActors() != null) {
                                uniqueActors.addAll(episode.getActors());
                            }
                        }
                    }
                }
            }
        }
        // Return a new ArrayList to prevent modification of the original set
        return new ArrayList<>(uniqueActors);
    }

    /**
     * Gets all genres for this series
     *
     * @return List of genres
     */
    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }


    @Override
    public String toString() {
        return String.format(
            "%s (%d-%s, %d seasons, %d episodes, %.1f/10)",
            getTitle(),
            getStartYear(),
            !isOngoing() ? String.valueOf(getEndYear()) : "Ongoing",
            getTotalSeasons(),
            getTotalEpisodes(),
            getRating()
        );
    }

    public void setRating(double rating) {
        this.rating = rating;
    }


    public double getRating() {
        return rating;
    }

    /**
     * Sets the nominations for this series.
     * @param nominations A string containing nominations (comma-separated)
     */
    public void setNominations(String nominations) {
        if (nominations != null && !nominations.trim().isEmpty()) {
            this.awards = Arrays.stream(nominations.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            this.awards = new ArrayList<>();
        }
    }

    /**
     * Sets the awards for this series.
     * @param awards A string containing awards (comma-separated)
     */
    public void setAwards(String awards) {
        if (awards != null && !awards.trim().isEmpty()) {
            this.awards = Arrays.stream(awards.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            this.awards = new ArrayList<>();
        }
    }

    public String getCreator() {
        return director;
    }


    public void setActors(String[] actorNames) {
        this.actors = new ArrayList<>();
        for (String actorName : actorNames) {
            this.actors.add(new Actor(actorName));
        }
    }
}