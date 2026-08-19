package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.content.Series;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;


/**
 * Main controller for the advanced search functionality.
 * Coordinates between the search form and results table that are used to display the search results which are
 * built using the search criteria from the search form.
 */
public class AdvancedSearchController extends BaseSearchController {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedSearchController.class);


    public VBox searchForm;

    @FXML
    private TableView<Content> resultsTable;
    @FXML
    private TableColumn<Content, String> resultTitleColumn;
    @FXML
    private TableColumn<Content, String> resultTypeColumn;
    @FXML
    private TableColumn<Content, Integer> resultYearColumn;
    @FXML
    private TableColumn<Content, String> resultGenreColumn;
    @FXML
    private SearchFormController searchFormController;
    @FXML
    private ResultsTableController resultsTableController;
    @FXML
    private Button manageDetailsButton;
    @FXML
    private Label resultsCountLabel;

    private Task<ObservableList<Content>> currentSearchTask;
    private Map<String, Object> data;

    /**
     * Constructor for the AdvancedSearchController.
     * Initializes the search service and navigation service.
     */
    public AdvancedSearchController() {
        super(); // Initialize base controller
    }


    private TableColumn<Content, String> titleColumn;
    private TableColumn<Content, String> typeColumn;
    private TableColumn<Content, Integer> yearColumn;
    private TableColumn<Content, String> genreColumn;
    private TableColumn<Content, Integer> seasonsColumn;
    private TableColumn<Content, Integer> episodesColumn;

   
    private void initializeTableColumns() {
        try {
            logger.info("Initializing table columns...");
            
            // Clear existing columns to avoid duplicates
            resultsTable.getColumns().clear();
            
            // Configure Title Column
            if (resultTitleColumn != null) {
                resultTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                resultTitleColumn.setPrefWidth(200);
                resultTitleColumn.setStyle("-fx-font-weight: bold; -fx-text-fill: #00E5FF; -fx-font-size: 14;");
            }
            
            // Configure Type Column
            if (resultTypeColumn != null) {
                resultTypeColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    return new SimpleStringProperty(content instanceof Movie ? "Movie" : "Series");
                });
                resultTypeColumn.setPrefWidth(80);
                resultTypeColumn.setStyle("-fx-text-fill: #40C4FF; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
            
            // Configure Year Column
            if (resultYearColumn != null) {
                resultYearColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content != null && content.getReleaseDate() != null) {
                        int year = content.getReleaseDate().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .getYear();
                        return new javafx.beans.property.SimpleIntegerProperty(year).asObject();
                    }
                    return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
                });
                resultYearColumn.setCellFactory(column -> new TableCell<Content, Integer>() {
                    @Override
                    protected void updateItem(Integer year, boolean empty) {
                        super.updateItem(year, empty);
                        if (empty || year == null || year <= 0) {
                            setText("N/A");
                        } else {
                            setText(String.valueOf(year));
                        }
                        setStyle("-fx-text-fill: #69F0AE; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                });
                resultYearColumn.setPrefWidth(70);
            }
            
            // Configure Genre Column
            if (resultGenreColumn != null) {
                resultGenreColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content != null && content.getGenres() != null) {
                        String genres = content.getGenres().stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(" • "));
                        return new SimpleStringProperty(genres);
                    }
                    return new SimpleStringProperty("N/A");
                });
                resultGenreColumn.setPrefWidth(300);
                resultGenreColumn.setStyle("-fx-text-fill: #FF8A65; -fx-font-weight: bold;");
            }
            

            /*
             * Table column for displaying IMDb ratings.
             */
            TableColumn<Content, Double> ratingColumn = getContentDoubleTableColumn();

            // Seasons Column (initially hidden)
            seasonsColumn = new TableColumn<>("SEASONS");
            seasonsColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                if (content instanceof Series) {
                    return new javafx.beans.property.SimpleIntegerProperty(
                        ((Series) content).getSeasons() != null ? ((Series) content).getSeasons().size() : 0
                    ).asObject();
                }
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            });
            seasonsColumn.setCellFactory(column -> new TableCell<Content, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item == 0) {
                        setText("N/A");
                    } else {
                        setText(String.valueOf(item));
                    }
                    setStyle("-fx-text-fill: #80D8FF; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            });
            seasonsColumn.setPrefWidth(80);
            seasonsColumn.setVisible(false);
            
            // Episodes Column (initially hidden)
            episodesColumn = new TableColumn<>("EPISODES");
            episodesColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                if (content instanceof Series) {
                    return new javafx.beans.property.SimpleIntegerProperty(
                        ((Series) content).getTotalEpisodes()
                    ).asObject();
                }
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            });
            episodesColumn.setCellFactory(column -> new TableCell<Content, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item == 0) {
                        setText("N/A");
                    } else {
                        setText(String.valueOf(item));
                    }
                    setStyle("-fx-text-fill: #80D8FF; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            });
            episodesColumn.setPrefWidth(90);
            episodesColumn.setStyle("-fx-text-fill: #80D8FF; -fx-font-weight: bold; -fx-alignment: CENTER;");
            episodesColumn.setVisible(false);
            
            // Clear existing columns first
            resultsTable.getColumns().clear();
            
            // Add all columns to the table
            if (resultTitleColumn != null) resultsTable.getColumns().add(resultTitleColumn);
            if (resultTypeColumn != null) resultsTable.getColumns().add(resultTypeColumn);
            if (resultYearColumn != null) resultsTable.getColumns().add(resultYearColumn);
            if (resultGenreColumn != null) resultsTable.getColumns().add(resultGenreColumn);
            resultsTable.getColumns().add(ratingColumn);
            resultsTable.getColumns().add(seasonsColumn);
            resultsTable.getColumns().add(episodesColumn);
            
            // Set up the table's selection model
            resultsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            
            // Add double-click handler for table rows
            resultsTable.setRowFactory(tv -> {
                TableRow<Content> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Content content = row.getItem();
                        if (content != null) {
                            navigateToContentDetails(content);
                        }
                    }
                });
                return row;
            });
            
            logger.info("Table columns initialized successfully");
            
            // Add listener to show/hide columns based on selection
            resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    boolean isSeries = !(newSelection instanceof Movie);
                    seasonsColumn.setVisible(isSeries);
                    episodesColumn.setVisible(isSeries);
                }
            });
            
            logger.info("Table columns initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing table columns", e);
        }
    }

    private static TableColumn<Content, Double> getContentDoubleTableColumn() {
        TableColumn<Content, Double> ratingColumn = new TableColumn<>("RATING");
        ratingColumn.setCellValueFactory(cellData -> {
            Content content = cellData.getValue();
            double rating = content.getRating();
            return new javafx.beans.property.SimpleDoubleProperty(
                rating > 0 ? rating : 0.0
            ).asObject();
        });
        ratingColumn.setCellFactory(column -> new TableCell<Content, Double>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null || rating <= 0) {
                    setText("N/A");
                } else {
                    setText(String.format("%.1f", rating));
                }
                setStyle("-fx-text-fill: #FFD740; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });
        ratingColumn.setPrefWidth(70);
        return ratingColumn;
    }

    @FXML
    public void initialize() {
        try {
            logger.info("Initializing AdvancedSearchController...");
            
            // Initialize the table
            if (resultsTable != null) {
                logger.info("Initializing results table...");
                
                // Set up the table
                resultsTable.setVisible(true);
                resultsTable.setManaged(true);
                
                // Set up the selection model
                resultsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                
                // Update the manage details button state based on selection
                resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (manageDetailsButton != null) {
                        manageDetailsButton.setDisable(newSelection == null);
                    }
                });
                
                // Initialize table columns
                initializeTableColumns();
                
                logger.info("Results table initialized with {} columns", resultsTable.getColumns().size());
            } else {
                logger.error("Results table is not injected");
            }

            // Initialize the search form controller if available
            if (searchFormController != null) {
                logger.info("Initializing SearchFormController...");
                // Initialize the form with default values
                searchFormController.initializeForm();

                // Set up the search form listener
                searchFormController.setSearchFormListener(new SearchFormController.SearchFormListener() {
                    @Override
                    public void onSearchCriteriaChanged(SearchCriteria criteria) {
                        // Update UI based on search criteria changes if needed
                        if (hasSearchCriteria(criteria)) {
                            updateStatus("Ready to search");
                        } else {
                            updateStatus("Enter search criteria");
                        }
                    }

                    @Override
                    public void onSearchRequested(SearchCriteria criteria) {
                        logger.info("Search requested with criteria: {}", criteria);
                        // If criteria is null, create a new one with default sorting
                        if (criteria == null) {
                            criteria = new SearchCriteria("title", false);
                        }
                        performSearch(criteria);
                    }
                });
            } else {
                logger.error("Search form controller is not initialized");
            }

            // Initialize the results table and manage details button
            if (resultsTableController != null) {
                try {
                    TableView<Content> table = resultsTableController.getResultsTable();
                    if (table != null) {
                        // Set up selection model
                        TableView.TableViewSelectionModel<Content> selectionModel = table.getSelectionModel();
                        selectionModel.setSelectionMode(SelectionMode.SINGLE);

                        // Initialize the manage details button state
                        if (manageDetailsButton != null) {
                            // Initial state
                            manageDetailsButton.setDisable(true);

                            // Update button state when selection changes
                            selectionModel.selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                                manageDetailsButton.setDisable(newSelection == null);

                                // Update button style based on state
                                if (newSelection == null) {
                                    manageDetailsButton.setStyle(
                                        "-fx-background-color: transparent; " +
                                        "-fx-text-fill: #9E9E9E; " +
                                        "-fx-border-color: #9E9E9E; " +
                                        "-fx-border-width: 1.5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-padding: 5 10; " +
                                        "-fx-font-weight: bold;"
                                    );
                                } else {
                                    manageDetailsButton.setStyle(
                                        "-fx-background-color: transparent; " +
                                        "-fx-text-fill: #4CAF50; " +
                                        "-fx-border-color: #4CAF50; " +
                                        "-fx-border-width: 1.5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-padding: 5 10; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 5, 0, 0, 1);"
                                    );
                                }
                            });
                        }
                    }

                    // Set up the results table controller
                    if (resultsTableController != null) {
                        // Set up the table with the controller's table view
                        resultsTable = resultsTableController.getResultsTable();

                        // Set up double-click handler for table rows
                        resultsTable.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                handleTableClick();
                            }
                        });

                        // Bind the manage details button's disable property to the table's selection model
                        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                            if (newSelection != null) {
                                // Enable the manage details button when an item is selected
                                manageDetailsButton.setDisable(false);
                            }
                        });
                    }

                    updateStatus("Enter search criteria");
                } catch (Exception e) {
                    logger.error("Error initializing table components", e);
                    showError("Initialization Error", "Failed to initialize table components: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error initializing AdvancedSearchController", e);
            showError("Initialization Error", "Failed to initialize search interface: " + e.getMessage());
        }
    }

    /**
     * Checks if the search criteria contains any search terms.
     *
     * @param criteria The search criteria to check
     * @return true if the criteria contains search terms, false otherwise
     */
    private boolean hasSearchCriteria(SearchCriteria criteria) {
        if (criteria == null) {
            return false;
        }
        
        // Check for search terms in title or query (not empty and not just whitespace)
        if ((criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) ||
            (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty())) {
            return true;
        }
        
        // Check for valid year range (at least one year specified)
        if (criteria.getMinYear() != null || criteria.getEndYear() != null) {
            // If either year is specified, it's a valid search
            return true;
        }
        
        // Check for rating (greater than 0 or less than 10)
        if ((criteria.getMinImdbRating() != null && criteria.getMinImdbRating() > 0) || 
            (criteria.getMaxImdbRating() != null && criteria.getMaxImdbRating() < 10)) {
            return true;
        }
        
        // Check for content type (both checkboxes can't be unchecked)
        if (criteria.getContentType() != null) {
            return true;
        }
        
        // Check for genres (at least one genre selected)
        return criteria.getGenres() != null && !criteria.getGenres().isEmpty();
        
        // No valid search criteria found
    }

    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            navigationService.navigateTo(
                "/fxml/base/home-view.fxml",
                    data, getStage(),
                "IMDb Clone - Home"
            );
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }

   
    /**
     * Performs a search with the given criteria.
     *
     * @param criteria The search criteria to use
     */
    private void performSearch(SearchCriteria criteria) {
        if (criteria == null) {
            logger.warn("Search criteria is null");
            showInfo("Search", "No search criteria provided");
            // Clear existing results
            if (resultsTable != null) {
                resultsTable.getItems().clear();
                resultsTable.refresh();
            }
            if (resultsCountLabel != null) {
                resultsCountLabel.setText("No search criteria provided");
            }
            return;
        }

        // Log the search criteria for debugging
        logger.info("Performing search with criteria: {}", criteria);

        // Cancel any ongoing search
        if (currentSearchTask != null && currentSearchTask.isRunning()) {
            logger.info("Cancelling previous search task");
            currentSearchTask.cancel();
        }

        // Validate search service
        if (searchService == null) {
            logger.error("Search service is not available");
            showError("Error", "Search functionality is not available");
            return;
        }

        // Make sure the results table is available
        if (resultsTable == null) {
            logger.error("Results table is not available");
            showError("Error", "Search results cannot be displayed");
            return;
        }

        // Clear previous results and reset UI state
        resultsTable.getItems().clear();
        if (resultsCountLabel != null) {
            resultsCountLabel.setText("");
        }
        
        // Update status
        updateStatus("Searching...");
        
        // Make sure the table is visible and managed
        resultsTable.setVisible(true);
        resultsTable.setManaged(true);
        
        // Set up the table's selection model if not already done
        resultsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        resultsTable.getSelectionModel().clearSelection();

        // Disable the manage details button by default
        if (manageDetailsButton != null) {
            manageDetailsButton.setDisable(true);
        }
        
        // Log table state before search
        logger.debug("Table state before search - Items: {}, Columns: {}", 
            resultsTable.getItems().size(), 
            resultsTable.getColumns().size());

        // Create a new search task
        currentSearchTask = new Task<ObservableList<Content>>() {
            @Override
            protected ObservableList<Content> call() throws Exception {
                try {
                    // Perform the search using the search service
                    List<Content> results = searchService.search(criteria);
                    logger.info("Found {} results for search criteria: {}", results.size(), criteria);
                    
                    // Convert to observable list for JavaFX
                    ObservableList<Content> observableResults = FXCollections.observableArrayList(results);
                    
                    // Update the UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            // Update the table with search results
                            if (resultsTable != null) {
                                resultsTable.getItems().clear();
                                resultsTable.setItems(observableResults);
                                
                                // Update the results count label
                                if (resultsCountLabel != null) {
                                    String countText = results.isEmpty() ? "No results found" : 
                                        String.format("Found %d result%s", results.size(), results.size() != 1 ? "s" : "");
                                    resultsCountLabel.setText(countText);
                                }
                                
                                // Log the update
                                logger.info("Updated table with {} items", results.size());
                                
                                // Force refresh the table to show the new data
                                resultsTable.refresh();
                                
                                // Update the results table controller if available
                                if (resultsTableController != null) {
                                    resultsTableController.updateResults(observableResults);
                                }
                            }
                            
                            // Update status
                            updateStatus(String.format("Found %d results", results.size()));
                            
                        } catch (Exception e) {
                            logger.error("Error updating table with search results", e);
                            showError("Error", "Failed to display search results: " + e.getMessage());
                            updateStatus("Error displaying results");
                        }
                    });

                    return observableResults;
                    
                } catch (Exception e) {
                    logger.error("Error during search: {}", e.getMessage(), e);
                    Platform.runLater(() -> {
                        showError("Search Error", "Failed to perform search: " + 
                            (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                        updateStatus("Search failed");
                        
                        // Clear the results table and count label on error
                        if (resultsTable != null) {
                            resultsTable.getItems().clear();
                        }
                        
                        if (resultsTableController != null) {
                            resultsTableController.clearResults();
                        }
                        
                        if (resultsCountLabel != null) {
                            resultsCountLabel.setText("Search failed");
                        }
                    });
                    throw e;
                }
            }
        };

        // Handle successful search completion
        currentSearchTask.setOnSucceeded(event -> {
            try {
                // Results are already handled in the call() method through Platform.runLater
                // This is a fallback in case the Platform.runLater in call() didn't execute
                ObservableList<Content> results = currentSearchTask.getValue();
                if (results == null) {
                    logger.warn("Search completed but no results were returned");
                    updateStatus("No results found");
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText("No results found");
                    }
                    return;
                }

                // Update the results table controller
                if (resultsTableController != null) {
                    resultsTableController.setResults(results);
                    
                    // Update the results count label if not already updated
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", results.size()));
                    }
                    
                    updateStatus(String.format("Found %d results", results.size()));
                }
                
                logger.info("Search completed. Found {} results.", results.size());

                // Log the first few results for debugging
                int maxResultsToLog = Math.min(5, results.size());
                for (int i = 0; i < maxResultsToLog; i++) {
                    Content content = results.get(i);
                    logger.debug("Result {}: {} (ID: {})", i + 1, content.getTitle(), content.getId());
                }

                if (resultsTableController != null) {
                    // Make sure the table is visible
                    if (resultsTableController.getResultsTable() != null) {
                        resultsTableController.getResultsTable().setVisible(true);
                        logger.debug("Table visibility set to: {}", resultsTableController.getResultsTable().isVisible());
                    }

                    // Update the results using the controller's method
                    resultsTableController.updateResults(results);
                    logger.debug("Results passed to resultsTableController.updateResults()");

                    // Update the results count label
                    if (resultsCountLabel != null) {
                        String countText = String.format("Found %d result%s", results.size(), results.size() != 1 ? "s" : "");
                        resultsCountLabel.setText(countText);
                        logger.debug("Results count label updated to: {}", countText);
                    }

                    // Make sure the Manage Details button is properly set up
                    logger.debug("Table has {} items", resultsTable.getItems() != null ? resultsTable.getItems().size() : 0);
                    logger.debug("Table columns: {}", resultsTable.getColumns());

                    if (manageDetailsButton != null) {
                        // Update the button state based on current selection
                        manageDetailsButton.setDisable(resultsTable.getSelectionModel().getSelectedItem() == null);
                        logger.debug("Manage Details button disabled state: {}", manageDetailsButton.isDisabled());
                    }
                    updateStatus(String.format("Found %d results", results.size()));
                }
            } catch (Exception e){
                logger.error("Error performing search", e);
                showError("Search Error", "Failed to perform search: " + e.getMessage());
                updateStatus("Search failed");
            }
        });

        // Handle search errors
        currentSearchTask.setOnFailed(event -> {
            Throwable e = currentSearchTask.getException();
            String errorMsg = "Search failed: " + (e != null ? e.getMessage() : "Unknown error");
            logger.error(errorMsg, e);
            showError("Search Error", errorMsg);
            updateStatus("Search failed");
        });

        // Run the search in a background thread
        Thread searchThread = new Thread(currentSearchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }

    /**
     * Navigates to the content details view for the specified content.
     * Shows a detailed dialog with comprehensive content information when double-clicked.
     *
     * @param content The content to view details for
     */
    private void navigateToContentDetails(Content content) {
        if (content == null) {
            logger.warn("Attempted to navigate to null content");
            return;
        }

        try {
            // Create a dialog to show the content details
            Alert detailsDialog = new Alert(Alert.AlertType.INFORMATION);
            detailsDialog.setTitle("Content Details");
            detailsDialog.setHeaderText(null);
            detailsDialog.getDialogPane().setMinWidth(700);
            detailsDialog.getDialogPane().setMinHeight(500);

            // Set the owner stage for the dialog
            detailsDialog.initOwner(getStage());
            
            // Create a scroll pane to handle overflow
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setStyle("-fx-background: #f4f4f4; -fx-border-color: #e0e0e0;");

            // Main content container
            VBox contentBox = new VBox(15);
            contentBox.setPadding(new Insets(20));
            contentBox.setStyle("-fx-background-color: #ffffff;");

            // Create a styled header with title and year
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            String yearString = content.getYear() != null ? yearFormat.format(content.getYear()) : "N/A";
            
            // Header section
            VBox headerBox = new VBox(5);
            headerBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
            
            Label titleLabel = new Label(content.getTitle());
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #000000;");
            
            HBox metaBox = new HBox(15);
            Label yearLabel = new Label(yearString);
            yearLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            
            Label typeLabel = new Label(content instanceof Movie ? "Movie" : "TV Series");
            typeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            Label ratingLabel = new Label(String.format("★ %.1f/10", content.getRating()));
            ratingLabel.setStyle("-fx-text-fill: #f5c518; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            metaBox.getChildren().addAll(yearLabel, typeLabel, ratingLabel);
            headerBox.getChildren().addAll(titleLabel, metaBox);
            
            // Main content sections
            VBox detailsBox = new VBox(20);
            detailsBox.setPadding(new Insets(20, 0, 0, 0));
            
            // Genres section
            if (content.getGenres() != null && !content.getGenres().isEmpty()) {
                String genres = content.getGenres().stream()
                    .map(Enum::name)
                    .map(genre -> genre.charAt(0) + genre.substring(1).toLowerCase())
                    .collect(Collectors.joining(" • "));
                
                Label genresLabel = new Label("Genres: " + genres);
                genresLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14px; -fx-wrap-text: true;");
                genresLabel.setMaxWidth(Double.MAX_VALUE);
                detailsBox.getChildren().add(createSection("Genres", genresLabel));
            }
            

            // Cast section (placeholder - would be populated from content.getCast() in a real implementation)
            VBox castBox = new VBox(5);
            Label castTitle = new Label("Top Cast");
            castTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111; -fx-padding: 0 0 5 0;");
            
            // Sample cast members - in a real app, this would come from content.getCast()
            String[] sampleCast = {"Actor 1 as Character 1", "Actor 2 as Character 2", "Actor 3 as Character 3"};
            for (String castMember : sampleCast) {
                Label castMemberLabel = new Label("• " + castMember);
                castMemberLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14px;");
                castBox.getChildren().add(castMemberLabel);
            }
            
            // Add "See full cast" link
            VBox castSection = getVBox(castTitle, castBox);
            detailsBox.getChildren().add(castSection);
            
            // Details section
            VBox infoBox = new VBox(8);
            infoBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 4;");
            
            // Director
            if (content.getDirector() != null && !content.getDirector().isEmpty()) {
                infoBox.getChildren().add(createInfoRow("Director", content.getDirector()));
            }
            

            
            // Release date
            if (content.getReleaseDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
                infoBox.getChildren().add(createInfoRow("Release Date", dateFormat.format(content.getReleaseDate())));
            }
            
            // Runtime (for movies)
            if (content instanceof Movie && ((Movie) content).getRuntime() > 0) {
                int runtime = ((Movie) content).getRuntime();
                String runtimeStr = String.format("%d min", runtime);
                infoBox.getChildren().add(createInfoRow("Runtime", runtimeStr));
            }
            
            // Number of seasons and episodes (for series)
            if (content instanceof Series) {
                Series series = (Series) content;
                if (series.getSeasons() != null && !series.getSeasons().isEmpty()) {
                    infoBox.getChildren().add(createInfoRow("Seasons", String.valueOf(series.getSeasons().size())));
                    infoBox.getChildren().add(createInfoRow("Episodes", String.valueOf(series.getTotalEpisodes())));
                }
            }
            
            // Awards (if available)
            if (content.getAwards() != null && !content.getAwards().isEmpty()) {
                infoBox.getChildren().add(createInfoRow("Awards", String.valueOf(content.getAwards())));
            } else {
                infoBox.getChildren().add(createInfoRow("Awards", "N/A"));
            }
            
            // Box office (for movies)
            if (content instanceof Movie && ((Movie) content).getBoxOffice() != null && !((Movie) content).getBoxOffice().isEmpty()) {
                infoBox.getChildren().add(createInfoRow("Box Office", ((Movie) content).getBoxOffice()));
            }
            
            detailsBox.getChildren().add(createSection("Details", infoBox));
            
            // Assemble all sections
            contentBox.getChildren().addAll(headerBox, detailsBox);


            // Add buttons
            ButtonBar buttonBar = new ButtonBar();
            buttonBar.setPadding(new Insets(15, 0, 0, 0));

            Button editButton = getButton(content, detailsDialog);

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-padding: 8 16;");
            closeButton.setOnAction(e -> detailsDialog.close());
            
            buttonBar.getButtons().addAll(editButton, closeButton);
            ButtonBar.setButtonData(editButton, ButtonBar.ButtonData.OTHER);
            ButtonBar.setButtonData(closeButton, ButtonBar.ButtonData.CANCEL_CLOSE);
            
            VBox.setVgrow(detailsBox, Priority.ALWAYS);
            contentBox.getChildren().add(buttonBar);
            
            scrollPane.setContent(contentBox);
            detailsDialog.getDialogPane().setContent(scrollPane);
            
            // Set the dialog size
            detailsDialog.getDialogPane().setPrefSize(700, 600);
            
            // Show the dialog
            detailsDialog.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error showing content details", e);
            showError("Error", "Could not show content details: " + e.getMessage());
        }
    }

    private Button getButton(Content content, Alert detailsDialog) {
        Button editButton = new Button("Edit Content");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        editButton.setOnAction(e -> {
            detailsDialog.setResult(ButtonType.OK);
            detailsDialog.close();

            // Navigate to edit view
            Map<String, Object> data = new HashMap<>();
            data.put("contentId", content.getId());
            data.put("contentType", content.getContentType());

            navigationService.navigateTo(
                "/fxml/content/edit-content-view.fxml",
                data,
                getStage(),
                "Edit " + content.getTitle()
            );
        });
        return editButton;
    }

    private VBox getVBox(Label castTitle, VBox castBox) {
        Hyperlink fullCastLink = new Hyperlink("See full cast & crew");
        fullCastLink.setStyle("-fx-text-fill: #0073e6; -fx-font-size: 13px; -fx-padding: 5 0 0 0;");
        fullCastLink.setOnAction(e -> {
            // Would navigate to full cast view in a real implementation
            showInfo("Full Cast", "This would show the full cast and crew in a real implementation.");
        });

        VBox castSection = new VBox(10, castTitle, castBox, fullCastLink);
        return castSection;
    }

    /**
     * Creates a styled section with a title and content.
     */
    private VBox createSection(String title, Node content) {
        VBox section = new VBox(5);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111;");
        
        section.getChildren().addAll(titleLabel, content);
        return section;
    }
    
    /**
     * Creates a row for the info box with a label and value.
     */
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 100; -fx-text-fill: #555;");
        
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #333; -fx-wrap-text: true;");
        valueNode.setMaxWidth(Double.MAX_VALUE);
        
        HBox.setHgrow(valueNode, Priority.ALWAYS);
        row.getChildren().addAll(labelNode, valueNode);
        
        return row;
    }

    /**
     * Handles the manage details button click event.
     * Opens the edit view for the selected content item.
     */
    @FXML
    public void handleManageDetails() {
        try {
            // Get the selected content from the table
            Content selectedContent = null;
            if (resultsTableController != null && resultsTableController.getResultsTable() != null) {
                selectedContent = resultsTableController.getResultsTable().getSelectionModel().getSelectedItem();
            } else if (resultsTable != null && resultsTable.getSelectionModel() != null) {
                selectedContent = resultsTable.getSelectionModel().getSelectedItem();
            }
            
            if (selectedContent == null) {
                showInfo("No Selection", "Please select a valid item to manage details.");
                return;
            }
            
            logger.info("Opening edit view for content: {}", selectedContent.getTitle());
            
            // Create a map to pass data to the edit view
            Map<String, Object> data = new HashMap<>();
            data.put("contentId", selectedContent.getId());
            data.put("contentType", selectedContent.getContentType());
            
            // Navigate to the edit view with the content ID
            navigationService.navigateTo(
                "/fxml/content/edit-content-view.fxml",
                data,
                getStage(),
                "Edit " + selectedContent.getTitle()
            );
            
            logger.info("Successfully navigated to edit view for content ID: {}", selectedContent.getId());
            
        } catch (Exception e) {
            logger.error("Error navigating to edit content view: {}", e.getMessage(), e);
            showError("Error", "Failed to open edit view. Please try again.");
        }
    }
    
    /**
     * Handles the table click event.
     * If an item is selected, it will navigate to the content details.
     */
    @FXML
    public void handleTableClick() {
        if (resultsTable != null) {
            Content selectedContent = resultsTable.getSelectionModel().getSelectedItem();
            if (selectedContent != null) {
                navigateToContentDetails(selectedContent);
            }
        }
    }
}
