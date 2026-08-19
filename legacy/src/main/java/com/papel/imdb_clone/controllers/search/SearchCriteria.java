package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.enums.Genre;

import java.util.ArrayList;
import java.util.List;

/**
 * Search criteria such as title, year, rating, genre, content type, and sort order for filtering content.
 */
public class SearchCriteria {
    private Integer maxYear;
    private String query; // search query
    private String title; // title of the content
    private Double minImdbRating; // minimum IMDb rating
    private Double maxImdbRating; // maximum IMDb rating
    private Integer minYear; // minimum year
    private List<Genre> genres; // list of genres
    private Genre genre; // genre
    private ContentType contentType; // content type of media
    private String sortBy; // "title", "year", "rating", "duration"
    private boolean sortDescending; // sort order
    private Integer startYear; // start year
    private Integer endYear; // end year
    private String actor; // actor of the content
    private String director; // director of the content


    //search criteria constructor with query
    public SearchCriteria(String query) {
        this.query = query;
    }

    //search criteria constructor with all parameters
    public SearchCriteria(String query, Double minImdbRating, Double maxImdbRating, List<Genre> genres, Integer minYear, Integer maxYear, String sortBy, boolean sortDescending,
                          Integer startYear, Integer endYear, String actor, String director, ContentType contentType, Genre genre, String title) {
        this.query = query;
        this.minImdbRating = minImdbRating;
        this.maxImdbRating = maxImdbRating;
        this.genres = genres;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.sortBy = sortBy;
        this.sortDescending = sortDescending;
        this.startYear = startYear;
        this.endYear = endYear;
        this.actor = actor;
        this.director = director;
        this.contentType = contentType;
        this.genre = genre;
        this.title = title;
    }


    //search criteria constructor with sort by and sort descending
    public SearchCriteria(String sortBy, boolean sortDescending) {
        this.sortBy = sortBy;
        this.sortDescending = sortDescending;
    }


    //search criteria constructor with title and sort by
    public SearchCriteria(String title, String sortBy) {
        this.title = title;
        this.sortBy = sortBy;
    }

    //search criteria constructor with title, sort by, and sort descending
    public SearchCriteria(String title, String sortBy, boolean sortDescending) {
        this.title = title;
        this.sortBy = sortBy;
        this.sortDescending = sortDescending;
    }


    //set title
    public void setTitle(String title) {
        this.title = title;
    }

    //set min imdb rating
    public void setMinImdbRating(double rating) {
        this.minImdbRating = rating;
    }

    //set max imdb rating
    public void setMaxImdbRating(double rating) {
        this.maxImdbRating = rating;
    }

    //set genres
    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    //set sort field
    public void setSortField(String sortBy) {
        this.sortBy = sortBy;
    }
    
    // Alias for setSortField to match the method used in SearchFormController
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    //set sort order
    public void setSortOrder(String sortOrder) {
        this.sortDescending = sortOrder.equals("desc");
    }

    //get sort field
    public String getSortField() {
        return this.sortBy;
    }

    //get sort order
    public String getSortOrder() {
        return this.sortDescending ? "desc" : "asc";
    }

    //set min year
    public void setMinYear(Integer year) {
        this.minYear = year;
    }


    /**
     * Sets the minimum IMDb rating for filtering content.
     *
     * @param rating The minimum rating (0.0 to 10.0)
     * @throws IllegalArgumentException if rating is not between 0.0 and 10.0
     */
    public void setMinRating(double rating) {
        if (rating < 0.0 || rating > 10.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 10.0");
        }
        this.minImdbRating = rating;
    }


    //set genre
    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    //set content type
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    //set query
    public void setQuery(String query) {
        this.query = query;
    }

    //set max year
    public void setMaxYear(Integer yearToValue) {
        this.endYear = yearToValue;
    }

    // Getters
    public String getQuery() {
        return query;
    }

    public String getTitle() {
        return title;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public Genre getGenre() {
        return genre;
    }

    public Double getMinRating() {
        return minImdbRating;
    }

    public Integer getMinYear() {
        return minYear;
    }
    public Integer getMaxYear() {
        return endYear;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public Object getMaxRating() {
        return maxImdbRating;
    }

    public Integer getYear() {
        return minYear;
    }

    public String getDirector() {
        return null; // This method needs to be implemented based on your requirements
    }

    public String getActor() {
        return null; // This method needs to be implemented based on your requirements
    }
    
    public List<Genre> getGenres() {
        return genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    public Integer getEndYear() {
        return endYear;
    }

    /**
     * Returns a string representation of this {@code SearchCriteria} object.
     * The string representation includes the values of all fields for debugging purposes.
     *
     * @return A string representation of this {@code SearchCriteria} object.
     */
    @Override
    public String toString() {
        return "SearchCriteria{" +
                "title='" + title + '\'' +
                ", minImdbRating=" + minImdbRating +
                ", maxImdbRating=" + maxImdbRating +
                ", minYear=" + minYear +
                ", genres=" + genres +
                ", sortBy='" + sortBy + '\'' +
                ", sortDescending=" + sortDescending +
                ", endYear=" + endYear +
                ", startYear=" + startYear +
                ", contentType=" + contentType +
                ", genre=" + genre +
                ", query='" + query + '\'' +
                ", director='" + director + '\'' +
                ", actor='" + actor + '\'' +
                '}';
    }

    public Double getMinImdbRating() {
        return minImdbRating;
    }

    public Double getMaxImdbRating() {
        return maxImdbRating;
    }


    public void setMaxRating(double v) {
        this.maxImdbRating = v;
    }

    public boolean isSortDescending() {
        return sortDescending;
    }
}