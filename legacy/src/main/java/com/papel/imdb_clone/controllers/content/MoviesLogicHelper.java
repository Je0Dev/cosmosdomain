package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.service.content.MoviesService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import java.util.*;
import java.util.stream.Collectors;

public class MoviesLogicHelper {

    private final MoviesService moviesService;

    public MoviesLogicHelper() {
        this.moviesService = MoviesService.getInstance();
    }

    public ObservableList<Movie> getAllMovies() {
        return FXCollections.observableArrayList(moviesService.getAll());
    }

    public void initializeTableColumns(TableColumn<Movie, String> titleColumn,
            TableColumn<Movie, String> yearColumn,
            TableColumn<Movie, Integer> durationColumn,
            TableColumn<Movie, String> genreColumn,
            TableColumn<Movie, String> directorColumn,
            TableColumn<Movie, String> castColumn,
            TableColumn<Movie, Double> ratingColumn) {
        
        if (titleColumn != null) {
            titleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getTitle()));
        }
        
        if (yearColumn != null) {
            yearColumn.setCellValueFactory(cellData -> {
                Movie m = cellData.getValue();
                if (m.getReleaseDate() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(m.getReleaseDate());
                    return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
                }
                return new SimpleStringProperty("");
            });
        }
        
        if (durationColumn != null) {
            durationColumn.setCellValueFactory(cellData -> {
                Object val = cellData.getValue().getDuration();
                return new SimpleObjectProperty<>((Integer) val);
            });
        }
        
        if (genreColumn != null) {
            genreColumn.setCellValueFactory(cellData -> {
                Movie m = cellData.getValue();
                if (m.getGenres() != null && !m.getGenres().isEmpty()) {
                    return new SimpleStringProperty(m.getGenres().toString());
                }
                return new SimpleStringProperty("");
            });
        }
        
        if (directorColumn != null) {
            directorColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDirector()));
        }
        
        if (castColumn != null) {
            castColumn.setCellValueFactory(cellData -> {
                Movie m = cellData.getValue();
                if (m.getActors() != null && !m.getActors().isEmpty()) {
                    return new SimpleStringProperty(
                        m.getActors().stream()
                            .filter(a -> a != null)
                            .map(a -> a.getFullName())
                            .limit(5)
                            .collect(Collectors.joining(", ")));
                }
                return new SimpleStringProperty("");
            });
        }
        
        if (ratingColumn != null) {
            ratingColumn.setCellValueFactory(cellData -> {
                Double val = cellData.getValue().getImdbRating();
                return new SimpleObjectProperty<Double>(val);
            });
        }
    }

    public ObservableList<Movie> filterMovies(ObservableList<Movie> allMovies, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return allMovies;
        }
        
        String lower = searchText.toLowerCase();
        return allMovies.filtered(m -> 
            m.getTitle().toLowerCase().contains(lower) ||
            (m.getDirector() != null && m.getDirector().toLowerCase().contains(lower)));
    }

    public ObservableList<Movie> sortMovies(ObservableList<Movie> movies, String sortOption) {
        List<Movie> sorted = new ArrayList<>(movies);
        
        switch (sortOption) {
            case "Title (A-Z)":
                sorted.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Title (Z-A)":
                sorted.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Rating (Highest)":
                sorted.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
                break;
            case "Rating (Lowest)":
                sorted.sort((a, b) -> Double.compare(a.getRating(), b.getRating()));
                break;
        }
        
        return FXCollections.observableArrayList(sorted);
    }

    public void updateCounts(Label resultsCountLabel, Label itemCountLabel, int count) {
        if (resultsCountLabel != null) {
            Platform.runLater(() -> resultsCountLabel.setText(count + " results"));
        }
        if (itemCountLabel != null) {
            Platform.runLater(() -> itemCountLabel.setText(count + " items"));
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}