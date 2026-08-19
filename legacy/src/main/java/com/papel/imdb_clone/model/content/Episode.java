package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.content.Series;
import com.papel.imdb_clone.model.content.Season;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an episode of a TV show.
 */
public class Episode {
    private int id; //id for the episode
    private String director; //director of the episode
    private double imdbRating;//imdb rating of the episode
    private List<Actor> actors; //actors in the episode
    private String title;//title of the episode
    private int episodeNumber;//episode number of the episode
    private int seasonNumber;//season number of the episode
    private int seriesNumber;//series number of the episode
    private Date releaseDate;//release date of the episode
    private int duration;

    /**
     * Default constructor for Episode
     */
    public Episode() {
        this.id = 0;
        this.director = null;
        this.imdbRating = 0.0;
        this.actors = new ArrayList<>();
        this.title = "";
        this.episodeNumber = 0;
        this.seasonNumber = 0;
        this.seriesNumber = 0;
        this.releaseDate = new Date();
        this.duration = 0;
    }

    /**
     * Creates a new episode with essential information
     * @param episodeNumber The episode number within its season
     * @param title The title of the episode
     * @param series The series this episode belongs to
     * @param season The season this episode belongs to
     */
    public Episode(int episodeNumber, String title, Series series, Season season) {
        this();
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.seasonNumber = season.getSeasonNumber();
        this.seriesNumber = 1; // Default to 1 if not specified
        this.releaseDate = new Date(); // Default to current date
    }

    /**
     * Creates a new episode with all information
     * @param id The id of the episode
     * @param director  director of the episode
     * @param imdbRating imdbRating of the episode
     * @param actors actors in the episode
     * @param title title of the episode
     * @param episodeNumber episode number of the episode
     * @param seasonNumber season number of the episode
     * @param seriesNumber series number of the episode
     * @param releaseDate release date of the episode
     */
    public Episode(int id, String director, double imdbRating, List<Actor> actors, String title, int episodeNumber, int seasonNumber, int seriesNumber, Date releaseDate) {
        this.id = id;
        this.director = director;
        this.imdbRating = imdbRating;
        this.actors = actors;
        this.title = title;
        this.episodeNumber = episodeNumber;
        this.seasonNumber = seasonNumber;
        this.seriesNumber = seriesNumber;
        this.releaseDate = releaseDate;
    }


    public Episode(int episodeNumber, String episodeTitle) {
        this();
        this.episodeNumber = episodeNumber;
        this.title = episodeTitle;
    }

    public Episode(int episodeNumber, String episodeTitle, String director, double imdbRating, List<Actor> actors) {
        this();
        this.episodeNumber = episodeNumber;
        this.title = episodeTitle;
        this.director = director;
        this.imdbRating = imdbRating;
        this.actors = actors;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDirector() {
        return director;
    }
    
    public void setDirector(String director) {
        this.director = director;
    }

    public List<Actor> getActors() {
        return new ArrayList<>(actors);
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    @Override
    public String toString() {
        return "Episode{" +
                "id=" + id +
                ", director='" + (director != null ? director : "Unknown") + "'" +
                ", imdbRating=" + imdbRating +
                ", actors=" + actors.size() +
                ", title='" + (title != null ? title : "Unknown") + "'" +
                ", episodeNumber=" + episodeNumber +
                ", seasonNumber=" + seasonNumber +
                ", seriesNumber=" + seriesNumber +
                ", releaseDate=" + releaseDate +
                '}';
    }
}