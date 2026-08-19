package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.controllers.content.ContentDetailsController;
import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.content.Movie;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling the results table display and interactions.
 */
public class ResultsTableController extends BaseSearchController {
    private static final Logger logger = LoggerFactory.getLogger(ResultsTableController.class);

    /**
     * Constructs a new ResultsTableController.
     */
    public ResultsTableController() {
        super();
    }

    @FXML
    private TableView<Content> resultsTable;
    @FXML
    private TableColumn<Content, String> resultTitleColumn;
    @FXML
    private TableColumn<Content, Integer> resultYearColumn;
    @FXML
    private TableColumn<Content, String> resultTypeColumn;
    @FXML
    private TableColumn<Content, String> resultGenreColumn;
    @FXML
    private TableColumn<Content, String> resultImdbColumn;
    @FXML
    private TableColumn<Content, String> resultDirectorColumn;
    @FXML
    private TableColumn<Content, Integer> resultSeasonsColumn;
    @FXML
    private TableColumn<Content, Integer> resultEpisodesColumn;
    @FXML
    private Label resultsCountLabel;

    private final ObservableList<Content> searchResults = FXCollections.observableArrayList();

    /**
     * Gets the TableView instance.
     *
     * @return The TableView containing search results
     */
    public TableView<Content> getResultsTable() {
        return resultsTable;
    }

    @FXML
    public void initialize() {
        try {
            logger.info("Initializing ResultsTableController...");
            
            // Make sure the table is properly initialized
            if (resultsTable == null) {
                logger.error("Results table is null in initialize()");
                return;
            }
            
            // Set the items in the table first
            resultsTable.setItems(searchResults);
            
            // Set up the table columns
            setupTableColumns();
            
            // Set up the row double-click handler
            setupRowDoubleClickHandler();
            
            // Configure table properties with the recommended resize policy
            resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Set a custom resize policy that respects column constraints which helps to keep the table columns from getting too small or too large
            resultsTable.setColumnResizePolicy(tv -> {
                double width = resultsTable.getWidth();
                double totalWeight = resultsTable.getColumns().stream()
                    .mapToDouble(c -> {
                        Object constraint = c.getProperties().get("weight");
                        return constraint instanceof Number ? ((Number) constraint).doubleValue() : 1.0;
                    })
                    .sum();
                
                for (TableColumn<Content, ?> column : resultsTable.getColumns()) {
                    Object constraint = column.getProperties().get("weight");
                    double weight = constraint instanceof Number ? ((Number) constraint).doubleValue() : 1.0;
                    column.setPrefWidth(width * (weight / totalWeight));
                }
                return true;
            });
            resultsTable.setTableMenuButtonVisible(true);
            
            // Make sure the table is visible
            resultsTable.setVisible(true);
            
            // Log the table and column states
            logger.info("Table initialized with columns: {}", resultsTable.getColumns());
            logger.info("Table items count: {}", resultsTable.getItems().size());
            
            // Log column visibility
            if (resultsTable.getColumns() != null) {
                logger.info("Column visibility:");
                for (TableColumn<?, ?> col : resultsTable.getColumns()) {
                    logger.info("  - {}: visible={}, width={}", 
                        col.getText(), 
                        col.isVisible(),
                        col.getWidth());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error initializing ResultsTableController", e);
        }
    }

    /**
     * Sets the search results in the table.
     *
     * @param results The list of content items to display
     */
    public void setResults(ObservableList<Content> results) {
        if (results == null) {
            logger.warn("Attempted to set null results");
            searchResults.clear();
            return;
        }
        //Assert that results are not null which means the search was successful
        assert true;
        searchResults.setAll(results);
        updateResultsCount(results.size());
    }

    /**
     * Updates the table with new search results.
     *
     * @param results The list of content items to display
     */
    public void updateResults(List<Content> results) {
        logger.info("=== Starting updateResults ===");
        logger.info("Received results list: {} items", results != null ? results.size() : "null");
        
        // Make sure we're on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateResults(results));
            return;
        }
        
        try {
            //Assert that results are not null which means the search was successful
            assert true;
            if (results == null || results.isEmpty()) {
                logger.warn("No results to display - results list is {}", results == null ? "null" : "empty");
                searchResults.clear();
                updateResultsCount(0);
                
                // Make sure the table is visible even when there are no results
                if (resultsTable != null) {
                    resultsTable.setPlaceholder(new Label("No results found"));
                }
                return;
            }
            
            // Log first few items to verify data
            int itemsToLog = Math.min(3, results.size());
            logger.info("First {} items in results:", itemsToLog);
            for (int i = 0; i < itemsToLog; i++) {
                Content item = results.get(i);
                logger.info("  {}. {} (ID: {})", i + 1, item.getTitle(), item.getId());
            }
            
            // Clear the current items and add all new items
            logger.debug("Updating search results (current size: {}, new size: {})", searchResults.size(), results.size());
            
            // Use setAll to update the observable list in one operation
            searchResults.setAll(results);
            
            // Verify the items were added
            logger.info("searchResults size after update: {}", searchResults.size());
            
            // Update the results count
            updateResultsCount(searchResults.size());
            
            // Check if we have any series in the results and log detailed info
            boolean hasSeries = false;
            int seriesCount = 0;
            for (Content content : results) {
                if (content instanceof com.papel.imdb_clone.model.content.Series) {
                    hasSeries = true;
                    seriesCount++;
                    com.papel.imdb_clone.model.content.Series series = (com.papel.imdb_clone.model.content.Series) content;
                    logger.debug("Found series: {} with {} seasons and {} total episodes", 
                        series.getTitle(), 
                        series.getSeasons().size(),
                        series.getSeasons().stream().mapToInt(s -> s.getEpisodes().size()).sum());
                }
            }
            
            logger.info("Found {} series in search results. Has series: {}", seriesCount, hasSeries);
            
            // Update column visibility based on content type
            if (resultSeasonsColumn != null) {
                logger.debug("Setting seasons column visibility to: {}", hasSeries);
                resultSeasonsColumn.setVisible(hasSeries);
            }
            if (resultEpisodesColumn != null) {
                logger.debug("Setting episodes column visibility to: {}", hasSeries);
                resultEpisodesColumn.setVisible(hasSeries);
            }
            
            // Make sure the table is visible
            if (resultsTable != null) {
                // Ensure the table is visible
                resultsTable.setVisible(true);
                
                // Log the table state for debugging
                for (String s : Arrays.asList("Table state after update:", 
                    "  - Table items: " + (resultsTable.getItems() != null ? resultsTable.getItems().size() : "null"), 
                    "  - Table columns: " + (resultsTable.getColumns() != null ? resultsTable.getColumns().size() : "null"),
                    "  - Has series: " + hasSeries,
                    "  - Seasons column visible: " + (resultSeasonsColumn != null ? resultSeasonsColumn.isVisible() : "N/A"),
                    "  - Episodes column visible: " + (resultEpisodesColumn != null ? resultEpisodesColumn.isVisible() : "N/A"))) {
                    logger.info(s);
                }

                // Force refresh of the table
                resultsTable.refresh();
                
                // Log the first item's data to verify it's properly bound
                if (!resultsTable.getItems().isEmpty()) {
                    Content firstItem = resultsTable.getItems().getFirst();
                    logger.debug("First item in table: {} (ID: {})", firstItem.getTitle(), firstItem.getId());
                }
            } else {
                logger.error("resultsTable is null in updateResults!");
            }
            
            logger.info("=== Finished updateResults ===");
        } catch (Exception e) {
            logger.error("Error updating results table", e);
            if (resultsTable != null) {
                resultsTable.setPlaceholder(new Label("Error loading results: " + e.getMessage()));
            }
        }
    }

    /**
     * Clears all results from the table.
     */
    public void clearResults() {
        searchResults.clear();
        updateResultsCount(0);
    }

    /**
     * Updates the results count label.
     *
     * @param count The number of results found
     */
    private void updateResultsCount(int count) {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(String.format("Found %d result%s", count, count != 1 ? "s" : ""));
        } else {
            logger.debug("Results count label is not available");
        }
    }

    /**
     * Sets up the table columns with appropriate cell value factories and cell factories.
     */
    private void setupTableColumns() {
        if (resultsTable == null) {
            logger.error("Results table is not initialized");
            return;
        }
        try {
            logger.debug("Setting up table columns...");
            
            // Set column weights(means how much space each column should take) for the custom resize policy
            resultTitleColumn.getProperties().put("weight", 3.0);
            resultYearColumn.getProperties().put("weight", 1.0);
            resultTypeColumn.getProperties().put("weight", 1.0);
            resultGenreColumn.getProperties().put("weight", 2.0);
            resultImdbColumn.getProperties().put("weight", 1.0);
            resultDirectorColumn.getProperties().put("weight", 2.0);
            if (resultSeasonsColumn != null) resultSeasonsColumn.getProperties().put("weight", 1.0);
            if (resultEpisodesColumn != null) resultEpisodesColumn.getProperties().put("weight", 1.0);
            
            // Set up title column
            if (resultTitleColumn != null) {
                logger.debug("Setting up title column");
                resultTitleColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    return new SimpleStringProperty(content != null ? content.getTitle() : "");
                });

                resultTitleColumn.setCellFactory(column -> new TableCell<>() {
                    private final Tooltip tooltip = new Tooltip();

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);
                            tooltip.setText(item);
                            tooltip.setStyle("-fx-font-size: 12px; -fx-padding: 5px;");
                            setTooltip(tooltip);
                        }
                    }
                });
            }

            // Set up type column
            if (resultTypeColumn != null) {
                logger.debug("Setting up type column");
                resultTypeColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content == null) return new SimpleStringProperty("");
                    String type = content instanceof Movie ? "Movie" : "Series";
                    return new SimpleStringProperty(type);
                });
                
                // Column is already defined in FXML, no need to add it again
            }

            // Set up year column
            if (resultYearColumn != null) {
                logger.debug("Setting up year column");
                resultYearColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    return content != null ? 
                           new SimpleIntegerProperty(content.getStartYear()).asObject() : 
                           new SimpleIntegerProperty(0).asObject();
                });
                
                // Column is already defined in FXML, no need to add it again
            }

            // Set up genre column
            if (resultGenreColumn != null) {
                logger.debug("Setting up genre column");
                resultGenreColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content == null || content.getGenres() == null) {
                        return new SimpleStringProperty("");
                    }

                    String genres = content.getGenres().stream()
                            .map(Enum::name)
                            .map(name -> name.charAt(0) + name.substring(1).toLowerCase())
                            .collect(Collectors.joining(", "));

                    return new SimpleStringProperty(genres);
                });
                
                // Column is already defined in FXML, no need to add it again
            }

            // Set up IMDb rating column
            if (resultImdbColumn != null) {
                logger.debug("Setting up IMDb rating column");
                resultImdbColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content == null || content.getImdbRating() == null || content.getImdbRating() <= 0) {
                        return new SimpleStringProperty("N/A");
                    }
                    return new SimpleStringProperty(String.format("%.1f", content.getImdbRating()));
                });
                
                // Column is already defined in FXML, no need to add it again
            }

            // Set up director column
            if (resultDirectorColumn != null) {
                logger.debug("Setting up director column");
                resultDirectorColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    String director = (content != null && content.getDirector() != null) ?
                            content.getDirector() : "N/A";
                    return new SimpleStringProperty(director);
                });
            }
            
            // Set up seasons column
            if (resultSeasonsColumn != null) {
                logger.debug("Setting up seasons column");
                resultSeasonsColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content instanceof com.papel.imdb_clone.model.content.Series) {
                        com.papel.imdb_clone.model.content.Series series = (com.papel.imdb_clone.model.content.Series) content;
                        int seasonCount = series.getSeasons().size();
                        logger.trace("Series '{}' has {} seasons", series.getTitle(), seasonCount);
                        return new SimpleIntegerProperty(seasonCount).asObject();
                    }
                    return new SimpleIntegerProperty(0).asObject();
                });
                
                // Add cell factory to handle the display
                resultSeasonsColumn.setCellFactory(column -> new TableCell<Content, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(String.valueOf(item));
                            setStyle("-fx-alignment: CENTER;");
                        }
                    }
                });
                
                // Initialize as not visible
                resultSeasonsColumn.setVisible(false);
            }
            
            // Set up episodes column
            if (resultEpisodesColumn != null) {
                logger.debug("Setting up episodes column");
                resultEpisodesColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content instanceof com.papel.imdb_clone.model.content.Series series) {
                        int episodeCount = series.getSeasons().stream()
                                .mapToInt(s -> s.getEpisodes().size())
                                .sum();
                        logger.trace("Series '{}' has {} total episodes", series.getTitle(), episodeCount);
                        return new SimpleIntegerProperty(episodeCount).asObject();
                    }
                    return new SimpleIntegerProperty(0).asObject();
                });
                
                // Add cell factory to handle the display
                resultEpisodesColumn.setCellFactory(column -> new TableCell<Content, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(String.valueOf(item));
                            setStyle("-fx-alignment: CENTER;");
                        }
                    }
                });
                
                // Initialize as not visible
                resultEpisodesColumn.setVisible(false);
            }
        } catch (Exception e) {
            logger.error("Error setting up table columns", e);
        }
    }

    /**
     * Shows the content details for the selected content item.
     *
     * @param content The content to show details for
     */
    protected void showContentDetails(Content content) {
        if (content == null) {
            logger.warn("Cannot show details for null content");
            return;
        }

        try {
            // Create data map with content ID
            Map<String, Object> contentData = new HashMap<>();
            contentData.put("contentId", content.getId());
            contentData.put("contentType", content.getContentType());
            
            // Navigate to content details view
            String fxmlPath = "/fxml/content/content-details-view.fxml";
            navigationService.navigateTo(fxmlPath, contentData, getStage(), content.getTitle());

            // Get the controller and pass the content
            ContentDetailsController controller = (ContentDetailsController) navigationService.getCurrentController();
            if (controller != null) {
                controller.setContent(content);
            }
        } catch (Exception e) {
            logger.error("Error navigating to content details", e);
            showError("Navigation Error", "Could not open content details: " + e.getMessage());
        }
    }


    /**
     * Shows a detailed dialog with content information.
     *
     * @param content The content to show details for
     */
    private void showContentDetailsDialog(Content content) {
        try {
            // Create a dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(content.getTitle());
            dialog.setHeaderText(content.getTitle() + " (" + content.getStartYear() + ")");

            // Create content
            VBox contentBox = new VBox(10);
            contentBox.setPadding(new Insets(10));
            
            // Add basic info
            Label typeLabel = new Label("Type: " + (content instanceof Movie ? "Movie" : "Series"));
            Label yearLabel = new Label("Year: " + content.getStartYear());
            
            // Add genres
            String genres = content.getGenres() != null ? 
                content.getGenres().stream()
                    .map(Enum::name)
                    .map(name -> name.charAt(0) + name.substring(1).toLowerCase())
                    .collect(Collectors.joining(", ")) : "N/A";
            Label genresLabel = new Label("Genres: " + genres);
            
            // Add rating
            Label ratingLabel = new Label(String.format("IMDb Rating: %s", 
                content.getImdbRating() != null && content.getImdbRating() > 0 ? 
                String.format("%.1f/10", content.getImdbRating()) : "N/A"));
            
            // Add director if available
            String director = content.getDirector() != null ? content.getDirector() : "N/A";
            Label directorLabel = new Label("Director: " + director);
            
            // Create cast label with bold text
            Label castLabel = new Label();
            String castText = content.getCast() != null ? content.getCast().toString() : "N/A";
            castLabel.setText("Cast: " + castText);
            castLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
            
            // Add awards (placeholder - you'll need to implement getAwards() in your Content model)
            Label awardsLabel = new Label("Awards: " + 
                (content.getAwards() != null ? content.getAwards() : "N/A"));
            
            // Add all elements to content box
            contentBox.getChildren().addAll(
                typeLabel, 
                yearLabel, 
                genresLabel, 
                ratingLabel, 
                directorLabel,
                new Separator(),
                new Label("Cast:"),
                castLabel,
                new Separator(),
                new Label("Awards:"),
                awardsLabel
            );
            
            // Add content to dialog
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(contentBox);
            
            // Add close button
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().add(closeButton);
            
            // Show dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error showing content details dialog", e);
            showError("Error", "Could not show content details: " + e.getMessage());
        }
    }

    /**
     * Sets up the row click handlers for the results table.
     */
    private void setupRowDoubleClickHandler() {
        if (resultsTable == null) {
            logger.warn("Cannot set up row click handler: results table is null");
            return;
        }

        resultsTable.setRowFactory(tv -> {
            TableRow<Content> row = new TableRow<>();

            // Handle single click for quick details
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Content content = row.getItem();
                    if (event.getClickCount() == 1) {
                        // Show quick details on single click
                        showContentDetailsDialog(content);
                    } else if (event.getClickCount() == 2) {
                        // Open full details on double click
                        showContentDetails(content);
                    }
                }
            });

            // Add hover effect that helps with user experience
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (isNowHovered && !row.isEmpty()) {
                    row.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;");
                } else {
                    row.setStyle("");
                }
            });

            return row;
        });
    }
}
