package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;

import java.util.*;

/**
 * Represents a movie.
 */
public class Movie extends Content {

    private String boxOffice; // Box office revenue
    private List<String> awards; // Awards received
    private List<Genre> genres = new ArrayList<>(); // Multiple genres support
    private Date releaseDate; // Release date
    private double imdbRating; // IMDB rating
    private String director;//Director
    private List<Actor> actors; //Actors
    private int startyear;//Start year
    private int endyear;//End year
    private double rating; //Rating

    private Integer userRating; //User rating
    private int duration;


    //default constructor
    public Movie() {
        super("Movie",
                new Date(124, Calendar.JANUARY, 1), // 2024 - 1900 = 124 (years since 1900)
                null,
                "Unknown",
                new HashMap<>(),
                0.0);
        // Initialize fields directly instead of calling methods that might use 'this'
        this.awards = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.imdbRating = 0.0;
        this.userRating = null;
        this.startyear = 0;
        this.endyear = 0;
        this.boxOffice = "";
        this.director = "";
    }

    //constructor for simple movie creation
    public Movie(String title, int year, String genre, String director, Map<Integer, Integer> userRatings, double imdbRating) {
        super(title, createDateFromYear(year), null, director, userRatings, imdbRating);

        this.releaseDate = createDateFromYear(year);
        this.awards = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.imdbRating = imdbRating;
        this.userRating = null;
        this.startyear = year;
        this.endyear = year;
        this.boxOffice = "";
        this.director = director;
        
        // Handle genre after object is fully initialized
        if (genre != null && !genre.trim().isEmpty()) {
            try {
                Genre genreEnum = Genre.valueOf(genre.toUpperCase().replace(" ", "_"));
                if (this.genres != null) {
                    this.genres.add(genreEnum);
                }
            } catch (IllegalArgumentException e) {
                // If genre is invalid, don't add any genre
                System.err.println("Invalid genre: " + genre);
            }
        }
    }

    // Helper method to create a Date from year
    private static Date createDateFromYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    // Method removed as it's no longer needed

    // Method to add a genre, ensuring thread safety and avoiding duplicates
    public void addGenre(Genre genre) {
        if (genre != null) {
            if (this.genres == null) {
                this.genres = new ArrayList<>();
                this.setGenres(this.genres);
            }
            if (!this.genres.contains(genre)) {
                this.genres.add(genre);
                this.setGenres(this.genres);
            }
        }
    }

    public Date getReleaseDate() {
        return releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    @Override
    public List<Actor> getActors() {
        // Return the list from the parent class to ensure we're using a single source of truth
        return super.getActors();
    }
    @Override
    public void setActors(List<Actor> actors) {
        // Create a new ArrayList with the provided actors (or empty list if null)
        List<Actor> newActors = actors != null ? new ArrayList<>(actors) : new ArrayList<>();
        // Use the parent class's setter to update the actors
        super.setActors(newActors);
    }

    @Override
    public String getDirector() {
        return super.getDirector();
    }

    public String getBoxOffice() {
        return boxOffice;
    }

    public void setBoxOffice(String boxOffice) {
        this.boxOffice = boxOffice;
    }

    public List<String> getAwards() {
        return new ArrayList<>(awards);
    }

    @Override
    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }
    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }


    @Override
    public String toString() {
        return "Movie{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", genre=" + getGenre() +
                ", actors=" + getActors().size() +
                ", director=" + getDirector() +
                ", boxOffice=" + getBoxOffice() +
                ", awards=" + getAwards() +
                ", imdbRating=" + getImdbRating() +
                ", genres=" + genres.size() +
                '}';
    }

    public void setAwards(String awards) {
        this.awards = Collections.singletonList(awards);
    }

    public void setRating(double rating) {
        this.setImdbRating(rating);
    }

    /**
     * Adds an actor to the movie's cast
     * @param actor The actor to add
     */
    public void addActor(Actor actor) {
        if (actor == null) {
            return;
        }
        
        // Get current actors and create a mutable copy
        List<Actor> currentActors = new ArrayList<>(getActors());
        
        // Add actor if not already in the list
        if (!currentActors.contains(actor)) {
            currentActors.add(actor);
            // Update the actors list with the new mutable list
            super.setActors(currentActors);
        }
    }


    public int getStartYear() {
        return startyear;
    }

    public void setStartYear(int year) {
        this.startyear = year;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Object getDuration() {
        return duration;
    }

    public int getRuntime() {
        return duration;
    }

}
