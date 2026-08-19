package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.model.content.Episode;
import com.papel.imdb_clone.model.content.Season;
import com.papel.imdb_clone.model.content.Series;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SeriesDialogHelper {

    private static final Logger logger = LoggerFactory.getLogger(SeriesDialogHelper.class);

    public Optional<Pair<Integer, String>> showAddSeasonDialog(int nextSeasonNumber) {
        Dialog<Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Add New Season");
        dialog.setHeaderText("Enter season details");

        ButtonType addButtonType = new ButtonType("Add", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField seasonNumberField = new TextField(String.valueOf(nextSeasonNumber));
        TextField titleField = new TextField();

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(false);

        seasonNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                seasonNumberField.setText(oldValue);
                return;
            }
            try {
                int seasonNum = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);
                addButton.setDisable(seasonNum <= 0);
            } catch (NumberFormatException e) {
                addButton.setDisable(true);
            }
        });

        grid.add(new Label("Season Number:"), 0, 0);
        grid.add(seasonNumberField, 1, 0);
        grid.add(new Label("Title (optional):"), 0, 1);
        grid.add(titleField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> {
            seasonNumberField.requestFocus();
            seasonNumberField.selectAll();
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    if (seasonNumberField.getText().trim().isEmpty()) {
                        return null;
                    }
                    int seasonNum = Integer.parseInt(seasonNumberField.getText().trim());
                    String title = titleField.getText().trim();
                    return new Pair<>(seasonNum, title);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        @SuppressWarnings("unchecked")
        Optional<Pair<Integer, String>> result = (Optional<Pair<Integer, String>>) (Optional<?>) dialog.showAndWait();
        return (Optional<Pair<Integer, String>>) (Optional<?>) result;
    }

    public Optional<Episode> showAddEpisodeDialog(Season selectedSeason, int nextEpisodeNumber) {
        Dialog<Episode> dialog = new Dialog<>();
        dialog.setTitle("Add Episode");
        dialog.setHeaderText("Enter episode details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField episodeNumberField = new TextField(String.valueOf(nextEpisodeNumber));
        TextField titleField = new TextField();
        TextField durationHoursField = new TextField("0");
        TextField durationMinutesField = new TextField("30");
        DatePicker releaseDatePicker = new DatePicker(LocalDate.now());

        grid.add(new Label("Episode Number:*"), 0, 0);
        grid.add(episodeNumberField, 1, 0);
        grid.add(new Label("Title:*"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Duration:*"), 0, 2);

        HBox durationBox = new HBox(5);
        durationBox.getChildren().addAll(
            durationHoursField,
            new Label("h"),
            durationMinutesField,
            new Label("m")
        );
        grid.add(durationBox, 1, 2);

        Label helpText = new Label("* Required fields. Duration must be at least 1 minute.");
        helpText.setStyle("-fx-text-fill: #666; -fx-font-size: 0.9em; -fx-padding: 5 0 0 0;");
        grid.add(helpText, 0, 4, 2, 1);

        grid.add(new Label("Release Date:"), 0, 3);
        grid.add(releaseDatePicker, 1, 3);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        ChangeListener<String> formValidationListener = (observable, oldValue, newValue) -> {
            try {
                boolean isValid = !episodeNumberField.getText().trim().isEmpty() &&
                                !titleField.getText().trim().isEmpty() &&
                                (!durationHoursField.getText().trim().isEmpty() || !durationMinutesField.getText().trim().isEmpty());

                if (isValid) {
                    int hours = durationHoursField.getText().trim().isEmpty() ? 0 : Integer.parseInt(durationHoursField.getText().trim());
                    int minutes = durationMinutesField.getText().trim().isEmpty() ? 0 : Integer.parseInt(durationMinutesField.getText().trim());
                    int totalMinutes = (hours * 60) + minutes;
                    isValid = totalMinutes > 0;
                }

                addButton.setDisable(!isValid);
            } catch (NumberFormatException e) {
                addButton.setDisable(true);
            }
        };

        episodeNumberField.textProperty().addListener(formValidationListener);
        titleField.textProperty().addListener(formValidationListener);
        durationHoursField.textProperty().addListener(formValidationListener);
        durationMinutesField.textProperty().addListener(formValidationListener);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(episodeNumberField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    if (episodeNumberField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Episode number cannot be empty");
                    }
                    int episodeNumber = Integer.parseInt(episodeNumberField.getText().trim());
                    String title = titleField.getText().trim();
                    if (title.isEmpty()) {
                        throw new IllegalArgumentException("Episode title cannot be empty");
                    }

                    int hours = durationHoursField.getText().trim().isEmpty() ? 0 : 
                              Integer.parseInt(durationHoursField.getText().trim());
                    int minutes = durationMinutesField.getText().trim().isEmpty() ? 0 : 
                                Integer.parseInt(durationMinutesField.getText().trim());

                    if (hours == 0 && minutes == 0) {
                        throw new IllegalArgumentException("Duration cannot be zero");
                    }

                    int totalMinutes = (hours * 60) + minutes;
                    LocalDate releaseDate = releaseDatePicker.getValue();
                    if (releaseDate == null) {
                        throw new IllegalArgumentException("Please select a valid release date");
                    }

                    Episode episode = new Episode(episodeNumber, title, null, selectedSeason);
                    episode.setDuration(totalMinutes);
                    episode.setReleaseDate(java.sql.Date.valueOf(releaseDate));
                    return episode;

                } catch (NumberFormatException e) {
                    logger.error("Error parsing episode number", e);
                    return null;
                } catch (IllegalArgumentException e) {
                    logger.error("Validation error", e);
                    return null;
                }
            }
            return null;
        });

        Optional<Episode> result = dialog.showAndWait();
        return result;
    }

    public boolean showSeriesEditDialog(Series series) {
        if (series == null) {
            return false;
        }

        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Series");
            dialog.setHeaderText("Edit series details: " + series.getTitle());
            dialog.setResizable(false);

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 10, 10, 10));

            TextField titleField = new TextField(series.getTitle());
            TextField startYearField = new TextField(String.valueOf(series.getStartYear()));
            TextField endYearField = new TextField(String.valueOf(series.getEndYear()));

            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Start Year:"), 0, 1);
            grid.add(startYearField, 1, 1);
            grid.add(new Label("End Year:"), 0, 2);
            grid.add(endYearField, 1, 2);

            dialog.getDialogPane().setContent(grid);

            Platform.runLater(titleField::requestFocus);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButtonType) {
                series.setTitle(titleField.getText().trim());
                try {
                    series.setStartYear(Integer.parseInt(startYearField.getText().trim()));
                    String endYearText = endYearField.getText().trim();
                    if (!endYearText.isEmpty()) {
                        series.setEndYear(Integer.parseInt(endYearText));
                    }
                } catch (NumberFormatException e) {
                    logger.error("Error parsing year", e);
                    return false;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error showing series edit dialog", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> showAdvancedSearchDialog() {
        Dialog dialog = new Dialog();
        dialog.setTitle("Advanced Search");
        dialog.setHeaderText("Enter your search criteria");

        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField startYearField = new TextField();
        startYearField.setPromptText("From Year");
        TextField endYearField = new TextField();
        endYearField.setPromptText("To Year");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Start Year:"), 0, 1);
        grid.add(startYearField, 1, 1);
        grid.add(new Label("End Year:"), 0, 2);
        grid.add(endYearField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == searchButtonType) {
                Map<String, Object> criteria = new HashMap<>();
                criteria.put("title", titleField.getText().trim());
                criteria.put("startYear", startYearField.getText().trim());
                criteria.put("endYear", endYearField.getText().trim());
                return (Map<String, Object>) (Map) criteria;
            }
            return null;
        });

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            return (Map<String, Object>) (Map) result.get();
        }
        return null;
    }
}