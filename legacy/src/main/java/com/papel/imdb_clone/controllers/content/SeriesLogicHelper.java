package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.model.content.Episode;
import com.papel.imdb_clone.model.content.Season;
import com.papel.imdb_clone.model.content.Series;
import com.papel.imdb_clone.service.content.SeriesService;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SeriesLogicHelper {

    private static final Logger logger = LoggerFactory.getLogger(SeriesLogicHelper.class);

    private final SeriesService seriesService;

    public SeriesLogicHelper() {
        this.seriesService = SeriesService.getInstance();
    }

    public ObservableList<Series> getAllSeries() {
        return FXCollections.observableArrayList(seriesService.getAll());
    }

    public void updateSeasonsTable(TableView<Season> seasonsTable, Series series) {
        if (series == null || series.getSeasons() == null) {
            if (seasonsTable != null) seasonsTable.getItems().clear();
            return;
        }
        if (seasonsTable != null) {
            seasonsTable.setItems(FXCollections.observableArrayList(series.getSeasons()));
        }
    }

    public void updateEpisodesTable(TableView<Episode> episodesTable, Season season) {
        if (season == null || season.getEpisodes() == null) {
            if (episodesTable != null) episodesTable.getItems().clear();
            return;
        }
        if (episodesTable != null) {
            episodesTable.setItems(FXCollections.observableArrayList(season.getEpisodes()));
        }
    }

    public void initializeSeasonsTable(TableColumn<Season, Integer> seasonNumberColumn,
            TableColumn<Season, String> seasonTitleColumn,
            TableColumn<Season, Integer> episodeCountColumn) {
        
        if (seasonNumberColumn != null) {
            seasonNumberColumn.setCellValueFactory(new PropertyValueFactory<>("seasonNumber"));
        }
        if (seasonTitleColumn != null) {
            seasonTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (episodeCountColumn != null) {
            episodeCountColumn.setCellValueFactory(cellData -> {
                int count = cellData.getValue().getEpisodes() != null ? 
                    cellData.getValue().getEpisodes().size() : 0;
                return new SimpleIntegerProperty(count).asObject();
            });
        }
    }

    public void initializeEpisodesTable(TableColumn<Episode, Integer> episodeNumberColumn,
            TableColumn<Episode, String> episodeTitleColumn,
            TableColumn<Episode, String> episodeDurationColumn) {
        
        if (episodeNumberColumn != null) {
            episodeNumberColumn.setCellValueFactory(new PropertyValueFactory<>("episodeNumber"));
        }
        if (episodeTitleColumn != null) {
            episodeTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (episodeDurationColumn != null) {
            episodeDurationColumn.setCellValueFactory(cellData -> {
                int mins = cellData.getValue().getDuration();
                return new SimpleStringProperty(formatDuration(mins));
            });
        }
    }

    public String formatDuration(int minutes) {
        if (minutes <= 0) return "N/A";
        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hours = minutes / 60;
            int mins = minutes % 60;
            if (mins == 0) {
                return hours + "h";
            } else {
                return hours + "h " + mins + "m";
            }
        }
    }

    public void initializeSeriesTable(TableColumn<Series, String> titleColumn,
            TableColumn<Series, Integer> startYearColumn,
            TableColumn<Series, String> endYearColumn) {
        
        if (titleColumn != null) {
            titleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getTitle()));
        }
        if (startYearColumn != null) {
            startYearColumn.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getStartYear()).asObject());
        }
        if (endYearColumn != null) {
            endYearColumn.setCellValueFactory(cellData -> {
                int year = cellData.getValue().getEndYear();
                return new SimpleStringProperty(year > 0 ? String.valueOf(year) : "N/A");
            });
        }
    }

    public List<Series> filterSeries(List<Series> seriesList, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return seriesList;
        }
        String lower = searchText.toLowerCase();
        return seriesList.stream()
            .filter(s -> s.getTitle().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public List<Series> sortSeries(List<Series> seriesList, String sortOption) {
        List<Series> sorted = new ArrayList<>(seriesList);
        switch (sortOption) {
            case "Title (A-Z)":
                sorted.sort(Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Title (Z-A)":
                sorted.sort(Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Year (Newest)":
                sorted.sort((a, b) -> Integer.compare(b.getStartYear(), a.getStartYear()));
                break;
            case "Year (Oldest)":
                sorted.sort((a, b) -> Integer.compare(a.getStartYear(), b.getStartYear()));
                break;
            case "Rating (Highest)":
                sorted.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
                break;
            case "Rating (Lowest)":
                sorted.sort((a, b) -> Double.compare(a.getRating(), b.getRating()));
                break;
        }
        return sorted;
    }

    public Map<String, Object> advancedSearch(String title, String startYear, String endYear,
            String genre, String status, String minRating, String maxRating) {
        
        Map<String, Object> params = new HashMap<>();
        
        if (title != null && !title.isEmpty()) {
            params.put("title", title);
        }
        if (startYear != null && !startYear.isEmpty()) {
            try { params.put("startYear", Integer.parseInt(startYear)); } catch (Exception e) {}
        }
        if (endYear != null && !endYear.isEmpty()) {
            try { params.put("endYear", Integer.parseInt(endYear)); } catch (Exception e) {}
        }
        if (minRating != null && !minRating.isEmpty()) {
            try { params.put("minRating", Double.parseDouble(minRating)); } catch (Exception e) {}
        }
        if (maxRating != null && !maxRating.isEmpty()) {
            try { params.put("maxRating", Double.parseDouble(maxRating)); } catch (Exception e) {}
        }
        
        return params;
    }

    public List<Series> performSearch(List<Series> allSeries, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return allSeries;
        }
        
        List<Series> results = allSeries.stream()
            .filter(s -> {
                if (params.containsKey("title")) {
                    String t = (String) params.get("title");
                    if (!s.getTitle().toLowerCase().contains(t.toLowerCase())) {
                        return false;
                    }
                }
                if (params.containsKey("startYear")) {
                    int y = (int) params.get("startYear");
                    if (s.getStartYear() < y) return false;
                }
                if (params.containsKey("endYear")) {
                    int y = (int) params.get("endYear");
                    if (s.getEndYear() > y || s.getEndYear() == 0) return false;
                }
                if (params.containsKey("minRating")) {
                    double r = (double) params.get("minRating");
                    if (s.getRating() < r) return false;
                }
                if (params.containsKey("maxRating")) {
                    double r = (double) params.get("maxRating");
                    if (s.getRating() > r) return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        return results;
    }

    public int getNextSeasonNumber(Series series) {
        if (series == null || series.getSeasons() == null) {
            return 1;
        }
        return series.getSeasons().stream()
            .mapToInt(Season::getSeasonNumber)
            .max()
            .orElse(0) + 1;
    }

    public int getNextEpisodeNumber(Season season) {
        if (season == null || season.getEpisodes() == null) {
            return 1;
        }
        return season.getEpisodes().stream()
            .mapToInt(Episode::getEpisodeNumber)
            .max()
            .orElse(0) + 1;
    }

    public void updateCounts(Label itemCountLabel, int totalCount) {
        if (itemCountLabel != null) {
            Platform.runLater(() -> itemCountLabel.setText(totalCount + " items"));
        }
    }

    public void updateStatus(Label statusLabel, String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }

    public void showSuccess(String title, String message) {
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