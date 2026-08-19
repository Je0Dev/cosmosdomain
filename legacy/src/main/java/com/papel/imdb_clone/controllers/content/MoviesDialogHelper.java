package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.content.Movie;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesDialogHelper {

    private static final Logger logger = LoggerFactory.getLogger(MoviesDialogHelper.class);

    public boolean showMovieEditDialog(Movie movie) {
        if (movie == null) {
            return false;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Movie".equals(movie.getTitle()) ? "Add New Movie" : "Edit Movie");
        dialog.setHeaderText("Enter movie details:");
        dialog.setResizable(false);
        dialog.setDialogPane(new DialogPane());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(movie.getTitle());
        TextField yearField = new TextField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        TextField directorField = new TextField(movie.getDirector() != null ? movie.getDirector() : "");
        TextField genreField = new TextField(movie.getGenres().stream()
            .map(Enum::name)
            .collect(Collectors.joining(", ")));
        TextField ratingField = new TextField(String.valueOf(movie.getRating()));
        String actorNames = movie.getActors().stream()
            .map(actor -> actor.getName().toString())
            .collect(Collectors.joining(", "));
        TextField actorsField = new TextField(actorNames);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Year:"), 0, 1);
        grid.add(yearField, 1, 1);
        grid.add(new Label("Director:"), 0, 2);
        grid.add(directorField, 1, 2);
        grid.add(new Label("Genre:"), 0, 3);
        grid.add(genreField, 1, 3);
        grid.add(new Label("Rating:"), 0, 4);
        grid.add(ratingField, 1, 4);
        grid.add(new Label("Actors:"), 0, 5);
        grid.add(actorsField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setMinHeight(300);

        Platform.runLater(titleField::requestFocus);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButtonType) {
            movie.setTitle(titleField.getText().trim());
            try {
                int year = Integer.parseInt(yearField.getText().trim());
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                movie.setReleaseDate(cal.getTime());
                movie.setRating(Double.parseDouble(ratingField.getText().trim()));
                movie.setGenres(Arrays.stream(genreField.getText().trim().split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .map(Genre::valueOf)
                        .collect(Collectors.toList()));
                movie.setActors(Arrays.stream(actorsField.getText().trim().split(","))
                        .map(String::trim)
                        .map(Actor::new)
                        .collect(Collectors.toList()));
            } catch (NumberFormatException e) {
                logger.error("Error parsing year", e);
                return false;
            }
            movie.setDirector(directorField.getText().trim());
            return true;
        }
        return false;
    }

    public boolean showMovieEditDialogWithError(Movie movie, Alert errorAlert) {
        if (movie == null) {
            return false;
        }

        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Movie".equals(movie.getTitle()) ? "Add New Movie" : "Edit Movie");
            dialog.setHeaderText("Enter movie details:");
            dialog.setResizable(false);

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField titleField = new TextField(movie.getTitle());
            TextField yearField = new TextField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            TextField directorField = new TextField(movie.getDirector() != null ? movie.getDirector() : "");
            TextField genreField = new TextField(movie.getGenres().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ")));
            TextField ratingField = new TextField(String.valueOf(movie.getRating()));
            TextField actorsField = new TextField(movie.getActors().stream()
                .map(actor -> actor.getName().toString())
                .collect(Collectors.joining(", ")));

            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Year:"), 0, 1);
            grid.add(yearField, 1, 1);
            grid.add(new Label("Director:"), 0, 2);
            grid.add(directorField, 1, 2);
            grid.add(new Label("Genre:"), 0, 3);
            grid.add(genreField, 1, 3);
            grid.add(new Label("Rating:"), 0, 4);
            grid.add(ratingField, 1, 4);
            grid.add(new Label("Actors:"), 0, 5);
            grid.add(actorsField, 1, 5);

            dialog.getDialogPane().setContent(grid);

            Platform.runLater(titleField::requestFocus);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == saveButtonType) {
                movie.setTitle(titleField.getText().trim());
                try {
                    int year = Integer.parseInt(yearField.getText().trim());
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    movie.setReleaseDate(cal.getTime());
                    movie.setRating(Double.parseDouble(ratingField.getText().trim()));
                    movie.setGenres(Arrays.stream(genreField.getText().trim().split(","))
                            .map(String::trim)
                            .map(String::toUpperCase)
                            .map(Genre::valueOf)
                            .collect(Collectors.toList()));
                    movie.setActors(Arrays.stream(actorsField.getText().trim().split(","))
                            .map(String::trim)
                            .map(Actor::new)
                            .collect(Collectors.toList()));
                } catch (NumberFormatException e) {
                    if (errorAlert != null) {
                        errorAlert.setTitle("Invalid Year");
                        errorAlert.setContentText("Please enter a valid year number.");
                        errorAlert.showAndWait();
                    }
                    logger.error("Error parsing year", e);
                    return false;
                }
                movie.setDirector(directorField.getText().trim());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error showing movie edit dialog", e);
            if (errorAlert != null) {
                errorAlert.setTitle("Error");
                errorAlert.setContentText("Failed to show movie editor: " + e.getMessage());
                errorAlert.showAndWait();
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> showAdvancedSearchDialog() {
        Dialog<Map> dialog = new Dialog<Map>();
        dialog.setTitle("Advanced Search");
        dialog.setHeaderText("Enter your search criteria");

        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField directorField = new TextField();
        directorField.setPromptText("Director");
        TextField yearFromField = new TextField();
        yearFromField.setPromptText("From Year");
        TextField yearToField = new TextField();
        yearToField.setPromptText("To Year");
        TextField ratingFromField = new TextField();
        ratingFromField.setPromptText("From Rating");
        TextField ratingToField = new TextField();
        ratingToField.setPromptText("To Rating");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Director:"), 0, 1);
        grid.add(directorField, 1, 1);
        grid.add(new Label("Year From:"), 0, 2);
        grid.add(yearFromField, 1, 2);
        grid.add(new Label("Year To:"), 0, 3);
        grid.add(yearToField, 1, 3);
        grid.add(new Label("Rating From:"), 0, 4);
        grid.add(ratingFromField, 1, 4);
        grid.add(new Label("Rating To:"), 0, 5);
        grid.add(ratingToField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == searchButtonType) {
                Map<String, Object> criteria = new HashMap<>();
                criteria.put("title", titleField.getText().trim());
                criteria.put("director", directorField.getText().trim());
                criteria.put("yearFrom", yearFromField.getText().trim());
                criteria.put("yearTo", yearToField.getText().trim());
                criteria.put("ratingFrom", ratingFromField.getText().trim());
                criteria.put("ratingTo", ratingToField.getText().trim());
                return (Map<String, Object>) (Map) criteria;
            }
            return null;
        });

        Optional<Map> result = dialog.showAndWait();
        if (result.isPresent()) {
            return (Map<String, Object>) (Map) result.get();
        }
        return null;
    }
}