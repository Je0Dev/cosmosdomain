package com.papel.imdb_clone.service.search;

import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.content.Series;

import java.util.*;
import java.util.stream.Collectors;

public class SearchCriteriaHelper {

    public List<Movie> filterMoviesByTitle(List<Movie> movies, String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>(movies);
        }
        String lower = title.toLowerCase();
        return movies.stream()
            .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public List<Movie> filterMoviesByDirector(List<Movie> movies, String director) {
        if (director == null || director.trim().isEmpty()) {
            return new ArrayList<>(movies);
        }
        String lower = director.toLowerCase();
        return movies.stream()
            .filter(m -> m.getDirector() != null && m.getDirector().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public List<Movie> filterMoviesByRating(List<Movie> movies, double minRating, double maxRating) {
        return movies.stream()
            .filter(m -> m.getImdbRating() >= minRating && m.getImdbRating() <= maxRating)
            .collect(Collectors.toList());
    }

    public List<Movie> filterMoviesByYear(List<Movie> movies, int yearFrom, int yearTo) {
        return movies.stream()
            .filter(m -> {
                int year = m.getStartYear();
                return year >= yearFrom && year <= yearTo;
            })
            .collect(Collectors.toList());
    }

    public List<Movie> sortMoviesByTitle(List<Movie> movies, boolean ascending) {
        List<Movie> sorted = new ArrayList<>(movies);
        if (ascending) {
            sorted.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else {
            sorted.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
        }
        return sorted;
    }

    public List<Movie> sortMoviesByRating(List<Movie> movies, boolean descending) {
        List<Movie> sorted = new ArrayList<>(movies);
        if (descending) {
            sorted.sort((a, b) -> Double.compare(b.getImdbRating(), a.getImdbRating()));
        } else {
            sorted.sort((a, b) -> Double.compare(a.getImdbRating(), b.getImdbRating()));
        }
        return sorted;
    }

    public List<Series> filterSeriesByTitle(List<Series> seriesList, String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>(seriesList);
        }
        String lower = title.toLowerCase();
        return seriesList.stream()
            .filter(s -> s.getTitle() != null && s.getTitle().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public List<Series> sortSeriesByTitle(List<Series> seriesList, boolean ascending) {
        List<Series> sorted = new ArrayList<>(seriesList);
        if (ascending) {
            sorted.sort(Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else {
            sorted.sort(Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
        }
        return sorted;
    }
}