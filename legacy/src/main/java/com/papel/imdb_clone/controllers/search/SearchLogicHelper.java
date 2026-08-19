package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.content.Series;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.util.*;
import java.util.stream.Collectors;

public class SearchLogicHelper {

    public List<Content> searchByTitle(String query, List<Movie> movies, List<Series> series) {
        if (query == null || query.trim().isEmpty()) {
            List<Content> all = new ArrayList<>();
            all.addAll(movies);
            all.addAll(series);
            return all;
        }
        
        String lower = query.toLowerCase();
        List<Content> results = new ArrayList<>();
        
        movies.stream()
            .filter(m -> m.getTitle().toLowerCase().contains(lower))
            .forEach(results::add);
        
        series.stream()
            .filter(s -> s.getTitle().toLowerCase().contains(lower))
            .forEach(results::add);
        
        return results;
    }

    public List<Content> filterByType(List<Content> items, String type) {
        if (type == null || type.equals("All")) {
            return items;
        }
        return items.stream()
            .filter(c -> c instanceof Movie && type.equals("Movies") ||
                       c instanceof Series && type.equals("Series"))
            .collect(Collectors.toList());
    }

    public List<Content> filterByRating(List<Content> items, double minRating, double maxRating) {
        return items.stream()
            .filter(c -> c.getRating() >= minRating && c.getRating() <= maxRating)
            .collect(Collectors.toList());
    }

    public List<Content> filterByYear(List<Content> items, int yearFrom, int yearTo) {
        return items.stream()
            .filter(c -> {
                int year = c.getStartYear();
                return year >= yearFrom && year <= yearTo;
            })
            .collect(Collectors.toList());
    }

    public List<Content> sortByTitle(List<Content> items, boolean ascending) {
        List<Content> sorted = new ArrayList<>(items);
        if (ascending) {
            sorted.sort(Comparator.comparing(Content::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else {
            sorted.sort(Comparator.comparing(Content::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
        }
        return sorted;
    }

    public List<Content> sortByRating(List<Content> items, boolean descending) {
        List<Content> sorted = new ArrayList<>(items);
        if (descending) {
            sorted.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
        } else {
            sorted.sort((a, b) -> Double.compare(a.getRating(), b.getRating()));
        }
        return sorted;
    }

    public void updateCounts(Label countLabel, int count) {
        if (countLabel != null) {
            Platform.runLater(() -> countLabel.setText(count + " results"));
        }
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}