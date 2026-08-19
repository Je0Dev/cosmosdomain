package com.papel.imdb_clone.model.people;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.service.people.CelebrityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an actor in the system.
 * Extends Celebrity with actor-specific fields and functionality.
 */
public class Actor extends Celebrity{

    //unchecked means that the compiler will not check the type of the object because it is not safe.This needed proper type checking like instanceof or getClass()
    @SuppressWarnings("unchecked")
    private static final CelebrityManager<Actor> celebrityManager = CelebrityManager.getInstance(Actor.class);
    
    private Ethnicity ethnicity;
    private String role; // Actor's role in a specific movie/show
    private final List<Movie> movies = new ArrayList<>(); // List of movies the actor has appeared in

    /**
     * Protected constructor to enforce use of factory methods.
     */
    public Actor(String firstName, String lastName, LocalDate birthDate, char gender, Ethnicity ethnicity) {
        super(firstName, lastName, birthDate, gender);
        this.ethnicity = ethnicity;
    }

    public Actor(String actorName) {
        super(actorName);
    }

    /**
     * Factory method to get or create an Actor instance.
     * @param firstName First name of the actor
     * @param lastName Last name of the actor
     * @param birthDate Birthdate (can be null)
     * @param gender Gender (M/F/other)
     * @return Existing or new Actor instance
     */
    public static Actor getInstance(String firstName, String lastName, LocalDate birthDate, char gender) {
        return getInstance(firstName, lastName, birthDate, gender, Ethnicity.UNKNOWN);
    }
    
    /**
     * Factory method with ethnicity.
     */
    public static Actor getInstance(String firstName, String lastName, LocalDate birthDate, 
                                  char gender, Ethnicity ethnicity) {
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        
        Actor temp = new Actor(firstName.trim(), lastName.trim(), birthDate, gender, ethnicity);
        return getInstance(temp);
    }
    
    /**
     * Internal factory method that handles the actual instance creation/lookup.
     */
    private static Actor getInstance(Actor actor) {
        return celebrityManager.findCelebrity(actor).orElseGet(() -> 
            celebrityManager.addCelebrity(actor)
        );
    }

    /**
     * Backward-compatible factory method with race/ethnicity label.
     */
    public static Actor getInstance(String firstName, String lastName, LocalDate birthDate, 
                                  char gender, String raceLabel) {
        Actor actor = getInstance(firstName, lastName, birthDate, gender);
        if (actor.ethnicity == null && raceLabel != null && !raceLabel.isBlank()) {
            try {
                actor.setEthnicity(Ethnicity.fromLabel(raceLabel));
            } catch (IllegalArgumentException ex) {
                // Keep existing ethnicity if any
            }
        }
        return actor;
    }

    // getter for ethnicity
    public Ethnicity getEthnicity() {
        return ethnicity;
    }
    
    /**
     * Gets the list of movies this actor has appeared in
     * @return List of movies
     */
    public List<com.papel.imdb_clone.model.content.Movie> getMovies() {
        return new ArrayList<>(movies);
    }
    
    /**
     * Adds a movie to this actor's filmography
     * @param movie The movie to add
     */
    public void addMovie(com.papel.imdb_clone.model.content.Movie movie) {
        if (movie != null && !movies.contains(movie)) {
            movies.add(movie);
        }
    }


    public void setEthnicity(Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }
    public String getRole() {
        return role != null ? role : "";
    }

    @Override
    public String toString() {
        return getFullName();
    }


    // setter for notable works
    @Override
    public void setNotableWorks(String notableWorks) {
        super.setNotableWorks(notableWorks);
    }

    //setters for first and lastname
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the list of notable works.
     * @return List of notable works, never null
     */
    @Override
    public List<String> getNotableWorks() {
        return super.getNotableWorks();
    }
    
    /**
     * Adds a notable work to the actor's list of notable works.
     * @param work The work to add
     */
    public void addNotableWork(String work) {
        if (work != null && !work.trim().isEmpty()) {
            super.addNotableWork(work);
        }
    }

    public Object getName() {
        return getFullName();
    }

    public String toLowerCase() {
        return getFullName().toLowerCase();
    }
}