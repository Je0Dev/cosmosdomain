package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.content.Episode;
import com.papel.imdb_clone.model.content.Season;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Series;

import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.Objects;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import java.util.stream.Collectors;

/**
 * Controller for managing TV series in the IMDB Clone application.
 */
public class SeriesController extends BaseController {

    /**
     * Logger instance for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(SeriesController.class);

    /**
     * Data map for storing application state
     */
    private Map<String, Object> data;

    /**
     * ID of the currently logged-in user
     */
    private int currentUserId;
    
    private final SeriesDialogHelper dialogHelper = new SeriesDialogHelper();
    private final SeriesLogicHelper logicHelper = new SeriesLogicHelper();

    @FXML private TableColumn<Series, Integer> seriesStartYearColumn;
    @FXML private TableColumn<Series, String> seriesEndYearColumn;
    @FXML private Button addSeasonButton;
    @FXML private Button addEpisodeButton;
    @FXML private TableView<Season> seasonsTable;
    @FXML private TableColumn<Season, Integer> seasonNumberColumn;
    @FXML private TableColumn<Season, String> seasonTitleColumn;
    @FXML private TableColumn<Season, Integer> episodeCountColumn;
    @FXML private TableView<Episode> episodesTable;
    @FXML private TableColumn<Episode, Integer> episodeNumberColumn;
    @FXML private TableColumn<Episode, String> episodeTitleColumn;
    @FXML private TableColumn<Episode, String> episodeDurationColumn;

    // Currently selected series and season
    private final ObjectProperty<Series> selectedSeries = new SimpleObjectProperty<>();
    private Season selectedSeason;

private void updateSeasonsTable() {
        logicHelper.updateSeasonsTable(seasonsTable, selectedSeries.get());
    }

    private void initializeSeasonsTable() {
        logicHelper.initializeSeasonsTable(seasonNumberColumn, seasonTitleColumn, episodeCountColumn);

        seasonsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedSeason = newSelection;
                addEpisodeButton.setDisable(newSelection == null);
                updateEpisodesTable();
            });
    }

    /**
     * Initializes the episodes table columns
     */
    private void initializeEpisodesTable() {
        logicHelper.initializeEpisodesTable(episodeNumberColumn, episodeTitleColumn, episodeDurationColumn);
    }

    private void updateEpisodesTable() {
        logicHelper.updateEpisodesTable(episodesTable, selectedSeason);
    }


    /**
     * Gets the next available season number for the selected series
     */
    private int getNextSeasonNumber() {
        if (selectedSeries == null || selectedSeries.get() == null) {
            return 1;
        }
        return selectedSeries.get().getSeasons().stream()
            .mapToInt(Season::getSeasonNumber)
            .max()
            .orElse(0) + 1;
    }

    /**
     * Handles adding a new season to the selected series
     */
    private void handleAddSeason() {
        if (selectedSeries == null || selectedSeries.get() == null) {
            showAlert("No Series Selected", "Please select a series first.");
            return;
        }

        try {
            // Create a dialog to get season details
            Dialog<Pair<Integer, String>> dialog = new Dialog<>();
            dialog.setTitle("Add New Season");
            dialog.setHeaderText("Enter season details");

            // Set the button types
            ButtonType addButtonType = new ButtonType("Add", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create the layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Auto-calculate next season number
            int nextSeasonNumber = getNextSeasonNumber();
            TextField seasonNumberField = new TextField(String.valueOf(nextSeasonNumber));
            TextField titleField = new TextField();
            
            // Get the Add button to enable/disable it based on input
            Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
            addButton.setDisable(false);

            // Add input validation for season number
            seasonNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    seasonNumberField.setText(oldValue);
                    return;
                }
                
                // Validate season number
                try {
                    int seasonNum = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);
                    boolean isValid = seasonNum > 0;
                    boolean isDuplicate = selectedSeries.get().getSeasons().stream()
                        .anyMatch(s -> s.getSeasonNumber() == seasonNum);
                    
                    // Update button state and field style
                    addButton.setDisable(!isValid || isDuplicate);
                    
                    if (isValid && isDuplicate) {
                        seasonNumberField.setStyle("-fx-text-fill: red;");
                        seasonNumberField.setTooltip(new Tooltip("A season with this number already exists"));
                    } else {
                        seasonNumberField.setStyle("");
                        seasonNumberField.setTooltip(null);
                    }
                } catch (NumberFormatException e) {
                    addButton.setDisable(true);
                }
            });

            grid.add(new Label("Season Number:"), 0, 0);
            grid.add(seasonNumberField, 1, 0);
            grid.add(new Label("Title (optional):"), 0, 1);
            grid.add(titleField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            
            // Set initial focus and select all text in season number field
            Platform.runLater(() -> {
                seasonNumberField.requestFocus();
                seasonNumberField.selectAll();
            });

            // Convert the result to a pair when the add button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        if (seasonNumberField.getText().trim().isEmpty()) {
                            showAlert("Missing Information", "Please enter a season number.");
                            return null;
                        }
                        
                        int seasonNumber = Integer.parseInt(seasonNumberField.getText().trim());
                        String title = titleField.getText().trim();
                        if (title.isEmpty()) {
                            title = "Season " + seasonNumber;
                        }
                        return new Pair<>(seasonNumber, title);
                    } catch (NumberFormatException e) {
                        showAlert("Invalid Input", "Please enter a valid season number.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Pair<Integer, String>> result = dialog.showAndWait();

            result.ifPresent(pair -> {
                try {
                    int seasonNumber = pair.getKey();
                    String title = pair.getValue();
                    
                    // Add the new season
                    Season newSeason = selectedSeries.get().addSeason(seasonNumber, title);
                    
                    // Update the series in the service
                    seriesService.update(selectedSeries.get());
                    
                    // Update the UI
                    updateSeasonsTable();
                    
                    // Select the new season
                    seasonsTable.getSelectionModel().select(newSeason);
                    selectedSeason = newSeason;
                    updateEpisodesTable();
                    
                    // Log the addition
                    logger.info("Added season {}: {}", seasonNumber, title);
                    
                    showSuccess("Success", String.format("Season %d added successfully!", seasonNumber));
                } catch (IllegalArgumentException e) {
                    showAlert("Error", e.getMessage());
                } catch (Exception e) {
                    logger.error("Error adding season: {}", e.getMessage(), e);
                    showAlert("Error", "Failed to add season: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Unexpected error in handleAddSeason: {}", e.getMessage(), e);
            showAlert("Error", "An unexpected error occurred while adding the season: " + e.getMessage());
        }
    }

    /**
     * Handles adding a new episode to the selected season
     */
    @FXML
    private void handleAddEpisode() {
        if (selectedSeason == null) {
            showError("No Season Selected", "Please select a season first.");
            return;
        }

        // Create a custom dialog
        Dialog<Episode> dialog = new Dialog<>();
        dialog.setTitle("Add New Episode");
        dialog.setHeaderText("Enter episode details");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField episodeNumberField = new TextField();
        TextField titleField = new TextField();
        TextField durationHoursField = new TextField();
        TextField durationMinutesField = new TextField();
        DatePicker releaseDatePicker = new DatePicker(LocalDate.now());
        
        // Set default values and prompt text
        durationHoursField.setPromptText("0");
        durationMinutesField.setPromptText("30");
        durationHoursField.setText("0");
        durationMinutesField.setText("30");
        

        // Add input validation for episode number
        episodeNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Only allow digits
                episodeNumberField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    int epNum = Integer.parseInt(newValue);
                    if (epNum <= 0) {
                        episodeNumberField.setText(oldValue);
                    }
                } catch (NumberFormatException e) {
                    episodeNumberField.setText(oldValue);
                }
            }
        });
        
        // Add input validation for hours
        durationHoursField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Only allow digits
                durationHoursField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    int hours = Integer.parseInt(newValue);
                    if (hours < 0 || hours > 24) {
                        durationHoursField.setText(oldValue);
                    }
                } catch (NumberFormatException e) {
                    durationHoursField.setText(oldValue);
                }
            }
        });
        
        // Add input validation for minutes
        durationMinutesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Only allow digits
                durationMinutesField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    int minutes = Integer.parseInt(newValue);
                    if (minutes < 0 || minutes >= 60) {
                        durationMinutesField.setText(oldValue);
                    }
                } catch (NumberFormatException e) {
                    durationMinutesField.setText(oldValue);
                }
            }
        });

        // Add components to grid
        grid.add(new Label("Episode Number:*"), 0, 0);
        grid.add(episodeNumberField, 1, 0);
        grid.add(new Label("Title:*"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Duration:*"), 0, 2);
        
        // Create a horizontal box for duration inputs
        HBox durationBox = new HBox(5);
        durationBox.getChildren().addAll(
            durationHoursField,
            new Label("h"),
            durationMinutesField,
            new Label("m")
        );
        grid.add(durationBox, 1, 2);
        
        // Add help text
        Label helpText = new Label("* Required fields. Duration must be at least 1 minute.");
        helpText.setStyle("-fx-text-fill: #666; -fx-font-size: 0.9em; -fx-padding: 5 0 0 0;");
        grid.add(helpText, 0, 4, 2, 1);
        
        grid.add(new Label("Release Date:"), 0, 3);
        grid.add(releaseDatePicker, 1, 3);

        // Enable/disable Add button based on input validation
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Add validation for the form
        ChangeListener<String> formValidationListener = (observable, oldValue, newValue) -> {
            try {
                boolean isValid = !episodeNumberField.getText().trim().isEmpty() &&
                                !titleField.getText().trim().isEmpty() &&
                                (!durationHoursField.getText().trim().isEmpty() || !durationMinutesField.getText().trim().isEmpty());
                
                if (isValid) {
                    int hours = durationHoursField.getText().trim().isEmpty() ? 0 : Integer.parseInt(durationHoursField.getText().trim());
                    int minutes = durationMinutesField.getText().trim().isEmpty() ? 0 : Integer.parseInt(durationMinutesField.getText().trim());
                    int totalMinutes = (hours * 60) + minutes;
                    isValid = totalMinutes > 0; // At least 1 minute duration
                }
                
                addButton.setDisable(!isValid);
            } catch (NumberFormatException e) {
                addButton.setDisable(true);
            }
        };

        // Add listeners to all input fields
        episodeNumberField.textProperty().addListener(formValidationListener);
        titleField.textProperty().addListener(formValidationListener);
        durationHoursField.textProperty().addListener(formValidationListener);
        durationMinutesField.textProperty().addListener(formValidationListener);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the episode number field by default
        Platform.runLater(episodeNumberField::requestFocus);

        // Convert the result to an Episode when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    // Validate episode number
                    if (episodeNumberField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Episode number cannot be empty");
                    }
                    int episodeNumber = Integer.parseInt(episodeNumberField.getText().trim());
                    
                    // Validate title
                    String title = titleField.getText().trim();
                    if (title.isEmpty()) {
                        throw new IllegalArgumentException("Episode title cannot be empty");
                    }
                    
                    // Validate duration
                    int hours = durationHoursField.getText().trim().isEmpty() ? 0 : 
                              Integer.parseInt(durationHoursField.getText().trim());
                    int minutes = durationMinutesField.getText().trim().isEmpty() ? 0 : 
                                Integer.parseInt(durationMinutesField.getText().trim());
                    
                    if (hours == 0 && minutes == 0) {
                        throw new IllegalArgumentException("Duration cannot be zero");
                    }
                    
                    int totalMinutes = (hours * 60) + minutes;
                    
                    // Validate release date
                    LocalDate releaseDate = releaseDatePicker.getValue();
                    if (releaseDate == null) {
                        throw new IllegalArgumentException("Please select a valid release date");
                    }
                    
                    // Create and return the episode
                    Episode episode = new Episode(episodeNumber, title, selectedSeries.get(), selectedSeason);
                    episode.setDuration(totalMinutes);
                    episode.setReleaseDate(java.sql.Date.valueOf(releaseDate));
                    return episode;
                    
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers for episode number and duration.");
                    return null;
                } catch (IllegalArgumentException e) {
                    showAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Episode> result = dialog.showAndWait();

        result.ifPresent(episode -> {
            try {
                // Check if episode number already exists
                boolean episodeExists = selectedSeason.getEpisodes().stream()
                    .anyMatch(e -> e.getEpisodeNumber() == episode.getEpisodeNumber());
                
                if (episodeExists) {
                    showError("Error", "An episode with this number already exists in this season.");
                    return;
                }
                
                try {
                    // Add the episode to the season
                    selectedSeason.addEpisode(episode);
                    
                    // Update the series in the service
                    seriesService.update(selectedSeries.get());
                    
                    // Update the UI
                    updateEpisodesTable();
                    
                    // Force refresh the seasons table to update episode count
                    updateSeasonsTable();
                    
                    showSuccess("Success", "Episode added successfully!");
                    
                    // Log the addition
                    logger.info("Added episode {}: {} ({} min) to season {}", 
                        episode.getEpisodeNumber(), 
                        episode.getTitle(), 
                        episode.getDuration(),
                        selectedSeason.getSeasonNumber());
                    
                } catch (Exception e) {
                    logger.error("Error adding episode: {}", e.getMessage(), e);
                    showError("Error", "Failed to add episode: " + e.getMessage());
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to add episode: " + e.getMessage());
            }
        });
    }

    /**
     * Handles the action when the manage series button is clicked.
     * Validates if a series is selected and shows the management dialog.
     *
     * @param event The action event that triggered this method
     */
    @FXML
    private void handleManageSeries(ActionEvent event) {
        try {
            Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
            if (selectedSeries != null) {
                //call showSeriesManagementDialog that will show the management dialog for the selected series
                showSeriesManagementDialog(selectedSeries);
            } else {
                showAlert("No Selection", "Please select a series to manage.");
            }

        } catch (Exception e) {
            logger.error("Error in handleManageSeries: {}", e.getMessage(), e);
            showError("Error", "Failed to manage series: " + e.getMessage());
        }
    }

    /**
     * Displays the series management dialog for the selected series.
     *
     * @param selectedSeries The series to be managed
     */
    private void showSeriesManagementDialog(Series selectedSeries) {
        try {
            //add message here
            showSeriesEditDialog(selectedSeries);
            loadSeries();
            showSuccess("Success", "Series managed successfully!");
        } catch (Exception e) {
            logger.error("Error in showSeriesManagementDialog: {}", e.getMessage(), e);
            showError("Error", "Failed to show series management: " + e.getMessage());
        }
    }


    private Label resultsCountLabel;
    @FXML
    private TableView<Series> seriesTable;
    @FXML
    private TableColumn<Series, String> seriesTitleColumn;
    @FXML
    private TableColumn<Series, String> seriesGenreColumn;
    @FXML
    private TableColumn<Series, Integer> seriesSeasonsColumn;
    @FXML
    private TableColumn<Series, Integer> seriesEpisodesColumn;
    @FXML
    private TableColumn<Series, Double> seriesRatingColumn;
    @FXML
    private TableColumn<Series, String> seriesCreatorColumn;
    @FXML
    private TableColumn<Series, String> seriesCastColumn;

    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            Stage stage;
            
            // Get the current stage from the scene graph
            if (seriesTable != null && seriesTable.getScene() != null) {
                stage = (Stage) seriesTable.getScene().getWindow();
            } else if (seasonsTable != null && seasonsTable.getScene() != null) {
                stage = (Stage) seasonsTable.getScene().getWindow();
            } else if (episodesTable != null && episodesTable.getScene() != null) {
                stage = (Stage) episodesTable.getScene().getWindow();
            } else if (addEpisodeButton != null && addEpisodeButton.getScene() != null) {
                stage = (Stage) addEpisodeButton.getScene().getWindow();
            } else {
                // Fallback to primary stage if no scene is available
                stage = ServiceLocator.getPrimaryStage();
            }
            
            // Clear any search filters or selections
            if (seriesSearchField != null) {
                seriesSearchField.clear();
            }
            
            // Navigate to home view
            navigationService.navigateTo("/fxml/base/home-view.fxml",
                    data, stage, "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            UIUtils.showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }

    @FXML
    private TextField seriesSearchField;
    @FXML
    private ComboBox<String> seriesSortBy;
    
    @FXML
    private Label selectedSeriesLabel;

    // Data
    /**
     * List containing all series loaded from the database
     */
    private final ObservableList<Series> allSeries = FXCollections.observableArrayList();

    /**
     * List containing series that match the current filter criteria
     */
    private final ObservableList<Series> filteredSeries = FXCollections.observableArrayList();
    
    private SeriesService seriesService;

    public SeriesController() {
        super();
        NavigationService navigationService = NavigationService.getInstance();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * by JavaFX after the FXML file has been loaded.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if not known
     * @param resources The resources used to localize the root object, or null if not localized
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This method is called by JavaFX during FXML loading
        // The actual initialization with user context happens in initializeController that is called by the main controller
    }

    /**
     * Initializes the controller with the current user's context.
     * This method sets up the controller with the current user's ID and initializes
     * the UI components and data bindings.
     *
     * @param currentUserId The ID of the currently logged-in user
     * @throws Exception if there is an error during initialization
     */
    @Override
    public void initializeController(int currentUserId) throws Exception {
        this.currentUserId = currentUserId;

        // Initialize dataManager through the service locator instead of calling super.initialize()
        this.dataManager = ServiceLocator.getInstance().getDataManager();

        // SeriesService is already initialized in the constructor

        // Set up the table
        setupTableColumns();

        // Bind the table to the filtered series list
        seriesTable.setItems(filteredSeries);

        // Load data and set up handlers
        loadSeries();
        setupSearchHandlers();
        setupSortHandlers();

        // Set up selection listener
        seriesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectedSeries.set(newSelection)
        );
        
        // Set up binding for the selected series
        selectedSeries.addListener((obs, oldSeries, newSeries) -> {
            if (newSeries != null) {
                updateSeasonsTable();
                Platform.runLater(() -> {
                    selectedSeriesLabel.setText(newSeries.getTitle() + " (" + newSeries.getStartYear() + ")");
                });
            } else {
                Platform.runLater(() -> {
                    selectedSeriesLabel.setText("No series selected");
                });
            }
        });

        // Add double-click handler to show episode counts
        seriesTable.setRowFactory(tv -> {
            TableRow<Series> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Series series = row.getItem();
                    showEpisodeCounts(series);
                }
            });
            return row;
        });

        // Initialize table columns for seasons and episodes
        initializeSeasonsTable();
        initializeEpisodesTable();

        // Set up event handlers
        setupEventHandlers();

        logger.info("SeriesController initialized with user ID: {}", currentUserId);
    }

    private void showEpisodeCounts(Series series) {
        if (series == null || series.getSeasons() == null || series.getSeasons().isEmpty()) {
            showAlert("No Seasons", "This series doesn't have any seasons yet.");
            return;
        }

        // Create a dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(String.format("%s - Episode Counts", series.getTitle()));
        dialog.setHeaderText("Number of episodes per season");

        // Create a table to show seasons and episode counts
        TableView<Season> table = new TableView<>();

        // Season number column
        TableColumn<Season, Integer> seasonCol = new TableColumn<>("Season");
        seasonCol.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getSeasonNumber()));

        // Episode count column
        TableColumn<Season, Integer> episodesCol = new TableColumn<>("Episodes");
        episodesCol.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getEpisodes() != null ?
                cellData.getValue().getEpisodes().size() : 0));

        // Add columns to table
        table.getColumns().add(seasonCol);
        table.getColumns().add(episodesCol);

        // Add data to table
        ObservableList<Season> seasons = FXCollections.observableArrayList(series.getSeasons());
        table.setItems(seasons);

        // Calculate total episodes
        int totalEpisodes = seasons.stream()
            .mapToInt(s -> s.getEpisodes() != null ? s.getEpisodes().size() : 0)
            .sum();

        // Add total row
        Label totalLabel = new Label(String.format("Total Episodes: %d", totalEpisodes));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        // Set up dialog layout
        VBox content = new VBox(10, table, totalLabel);
        content.setPadding(new Insets(10));

        // Set dialog content and buttons
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        // Set minimum size
        table.setPrefHeight(Math.min(400, 50 + (seasons.size() * 30)));

        // Show dialog
        dialog.showAndWait();
    }

    /**
     * Sets the series service for this controller.
     * This method allows for dependency injection of the SeriesService.
     *
     * @param seriesService The SeriesService instance to be used by this controller
     */
    public void setContentService(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    /**
     * Sets up event handlers for UI components
     */
    private void setupEventHandlers() {
        // Set up row selection handler
        seriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedSeries.set(newSelection);
                updateSeasonsTable();
            }
        });
        
        // Set up add season button
        addSeasonButton.setOnAction(event -> handleAddSeason());
        
        // Set up add episode button
        addEpisodeButton.setOnAction(event -> handleAddEpisode());
    }

    private void setupTableColumns() {
        // Set up title column
        seriesTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        seriesTitleColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up start year column
        seriesStartYearColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getStartYear()).asObject());
        seriesStartYearColumn.setCellFactory(col -> new TableCell<Series, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up end year column to show the end year when available, or "Ongoing" if the series is still running
        seriesEndYearColumn.setCellValueFactory(cellData -> {
            Series series = cellData.getValue();
            int endYear = series.getEndYear();
            int startYear = series.getStartYear();
            int currentYear = java.time.Year.now().getValue();
            
            // If end year is not set (0) or in the future, the series is ongoing
            if (endYear <= 0) {
                // If the start year is in the past, it's an ongoing series
                if (startYear > 0 && startYear <= currentYear) {
                    return new SimpleStringProperty("Ongoing");
                }
                // If no start year is set, show nothing
                return new SimpleStringProperty("");
            }
            
            // If end year is in the future, show as ongoing
            if (endYear > currentYear) {
                return new SimpleStringProperty("Ongoing");
            }
            
            // If end year is the same as start year, it's a miniseries or one-season show
            if (endYear == startYear) {
                return new SimpleStringProperty(String.valueOf(endYear));
            }
            
            // Otherwise, show the end year
            return new SimpleStringProperty(String.valueOf(endYear));
        });
        
        seriesEndYearColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("");
                    setStyle("");
                } else if (item.equals("Ongoing")) {
                    setText("Ongoing");
                    setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-style: italic; -fx-text-fill: #FFD700;");
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
                }
            }
        });

        // Set up genre column
        seriesGenreColumn.setCellValueFactory(cellData -> {
            List<String> genreNames = new ArrayList<>();
            for (Genre genre : cellData.getValue().getGenres()) {
                genreNames.add(genre.name());
            }
            return new SimpleObjectProperty<>(String.join(", ", genreNames));
        });
        seriesGenreColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up seasons column
        seriesSeasonsColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSeasons().size()));
        seriesSeasonsColumn.setCellFactory(col -> new TableCell<Series, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up episodes column
        seriesEpisodesColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalEpisodes()));
        seriesEpisodesColumn.setCellFactory(col -> new TableCell<Series, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up rating column
        seriesRatingColumn.setCellValueFactory(cellData -> {
            // First try to get the rating from the Series class (user rating)
            Double rating = cellData.getValue().getRating();
            // If no user rating, fall back to IMDb rating
            if (rating == 0.0) {
                rating = cellData.getValue().getImdbRating();
            }
            return new SimpleObjectProperty<>(rating);
        });

        // Set up rating column with enhanced display
        seriesRatingColumn.setCellFactory(col -> new TableCell<Series, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("N/A");
                    setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-text-fill: #888;");
                } else {
                    setText(String.format("%.1f", item));
                    // Color code based on rating
                    if (item >= 8.0) {
                        setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                    } else if (item >= 6.0) {
                        setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
                    } else {
                        setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #F44336;");
                    }
                }
            }
        });

        // Set up creator column
        seriesCreatorColumn.setCellValueFactory(new PropertyValueFactory<>("creator"));
        seriesCreatorColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up cast column
        seriesCastColumn.setCellValueFactory(cellData -> {
            String actors = "";
            if (cellData.getValue() != null && cellData.getValue().getActors() != null) {
                actors = cellData.getValue().getActors().stream()
                        .filter(Objects::nonNull)
                        .map(actor -> {
                            String name = (String) actor.getName();
                            return name != null ? name : "";
                        })
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.joining(", "));
            }
            return new SimpleStringProperty(actors);
        });
        // Set up cast column with enhanced display
        seriesCastColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10; -fx-wrap-text: true;");
                setWrapText(true);
            }
        });
    }

    // Set up search handlers that filter the series table based on the search field which could be title, creator, or actor
    private void setupSearchHandlers() {
        seriesSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterSeries());
        seriesSearchField.setPromptText("Search series by title");
    }

    // Set up sort handlers that sort the series table based on the sort field which could be title, year, rating etc.
    private void setupSortHandlers() {
        // Add sort options to the ComboBox
        seriesSortBy.getItems().addAll(
                "Title (A-Z)",
                "Title (Z-A)",
                "Year (Newest First)",
                "Year (Oldest First)",
                "Rating (Highest First)",
                "Rating (Lowest First)",
                "Seasons (Most First)",
                "Seasons (Fewest First)",
                "Episodes (Most First)",
                "Episodes (Fewest First)",
                "Creator (A-Z)",
                "Creator (Z-A)",
                "Actors (A-Z)",
                "Actors (Z-A)",
                "Genres (A-Z)",
                "Genres (Z-A)"
        );

        // Set default sort
        seriesSortBy.getSelectionModel().selectFirst();

        // Add listener for sort changes
        seriesSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sortSeriesTable(newVal);
            }
        });
    }

    // Load series from the service and display them in the table
    private void loadSeries() {
        if (seriesService == null) {
            //add message here
            Platform.runLater(() ->
                    statusLabel.setText("Error: Series service not initialized")
            );
            return;
        }

        try {
            // Get series from service that are not deleted
            List<Series> seriesList = seriesService.getAll();
            logger.info("Retrieved {} series from service", seriesList.size());

            Platform.runLater(() -> {
                try {
                    // Clear existing data
                    allSeries.clear();

                    // Use a Set to filter out duplicates based on title and start year
                    Set<String> uniqueSeriesKeys = new HashSet<>();
                    List<Series> uniqueSeries = new ArrayList<>();

                    for (Series series : seriesList) {
                        String key = series.getTitle().toLowerCase() + "_" + series.getStartYear();
                        if (uniqueSeriesKeys.add(key)) { // add returns true if the key was not already in the set
                            uniqueSeries.add(series);
                        } else {
                            logger.debug("Skipping duplicate series: {} ({})", series.getTitle(), series.getStartYear());
                        }
                    }

                    // Add only unique series to the observable list
                    allSeries.addAll(uniqueSeries);
                    logger.info("Added {} unique series to allSeries list ({} duplicates filtered out)",
                            uniqueSeries.size(), seriesList.size() - uniqueSeries.size());

                    // Update the filtered list and UI
                    filterSeries();

                    // Update results count
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", uniqueSeries.size()));
                    } else {
                        logger.warn("resultsCountLabel is not initialized");
                    }

                    statusLabel.setText(String.format("Loaded %d unique series", uniqueSeries.size()));
                    logger.info("Successfully loaded and displayed {} unique series", uniqueSeries.size());
                } catch (Exception e) {
                    logger.error("Error in Platform.runLater", e);
                    statusLabel.setText("Error updating UI: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error loading series", e);
            Platform.runLater(() ->
                    statusLabel.setText("Failed to load series: " + e.getMessage())
            );
        }
    }

    /*
     * Handle delete series action
     */
    @FXML
    private void handleDeleteSeries(ActionEvent event) {
        try {
            Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
            if (selectedSeries != null) {
                if (showConfirmationDialog("Confirm Deletion ", "Are you sure you want to delete this series?")) {
                    seriesService.delete(selectedSeries.getId());
                    loadSeries();
                    showSuccess("Success", "Series deleted successfully.");
                }
            } else {
                showAlert("No Selection", "Please select a series to delete.");
            }
        } catch (Exception e) {
            logger.error("Error deleting series", e);
            showError("Error", "Failed to delete series: " + e.getMessage());
        }
    }

    private void filterSeries() {
        String searchText = seriesSearchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filteredSeries.setAll(allSeries);
        } else {
            //update the filtered series list
            filteredSeries.setAll(allSeries.filtered(series -> {
                boolean titleMatch = series.getTitle().toLowerCase().contains(searchText);
                boolean creatorMatch = series.getCreator() != null && series.getCreator().toLowerCase().contains(searchText);
                boolean genreMatch = series.getGenres().stream()
                        .map(Enum::name)
                        .anyMatch(genre -> genre.toLowerCase().contains(searchText));
                return titleMatch || creatorMatch || genreMatch;
            }));
        }

        // Update the results count label
        if (resultsCountLabel != null) {
            int resultCount = filteredSeries.size();
            resultsCountLabel.setText(String.format("Results: %d", resultCount));

            // Update status label with search feedback
            if (!searchText.isEmpty()) {
                statusLabel.setText(String.format("Found %d series matching: %s", resultCount, searchText));
            }
        }

        seriesTable.setItems(filteredSeries);
    }

    private void sortSeriesTable(String sortOption) {
        if (sortOption == null) return;
        
        switch (sortOption) {
            case "Title (A-Z)":
                allSeries.sort(Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Title (Z-A)":
                allSeries.sort(Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Year (Newest First)":
                allSeries.sort(Comparator.comparingInt(Series::getStartYear).reversed());
                break;
            case "Year (Oldest First)":
                allSeries.sort(Comparator.comparingInt(Series::getStartYear));
                break;
            case "Rating (Highest First)":
                allSeries.sort(Comparator.comparingDouble(Series::getRating).reversed());
                break;
            case "Rating (Lowest First)":
                allSeries.sort(Comparator.comparingDouble(Series::getRating));
                break;
            case "Seasons (Most First)":
                allSeries.sort(Comparator.comparingInt(Series::getTotalSeasons).reversed());
                break;
            case "Seasons (Fewest First)":
                allSeries.sort(Comparator.comparingInt(Series::getTotalSeasons));
                break;
            case "Episodes (Most First)":
                allSeries.sort((s1, s2) -> 
                    Integer.compare(
                        s2.getSeasons().stream().mapToInt(Season::getEpisodesCount).sum(),
                        s1.getSeasons().stream().mapToInt(Season::getEpisodesCount).sum()
                    )
                );
                break;
            case "Episodes (Fewest First)":
                allSeries.sort(Comparator.comparingInt(s -> 
                    s.getSeasons().stream().mapToInt(Season::getEpisodesCount).sum()
                ));
                break;
            case "Creator (A-Z)":
                allSeries.sort(Comparator.comparing(Series::getCreator, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Creator (Z-A)":
                allSeries.sort(Comparator.comparing(Series::getCreator, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Genres (A-Z)":
                allSeries.sort((s1, s2) -> {
                    String g1 = s1.getGenres().isEmpty() ? "" : s1.getGenres().get(0).name();
                    String g2 = s2.getGenres().isEmpty() ? "" : s2.getGenres().get(0).name();
                    return g1.compareToIgnoreCase(g2);
                });
                break;
            case "Genres (Z-A)":
                allSeries.sort((s1, s2) -> {
                    String g1 = s1.getGenres().isEmpty() ? "" : s1.getGenres().get(0).name();
                    String g2 = s2.getGenres().isEmpty() ? "" : s2.getGenres().get(0).name();
                    return g2.compareToIgnoreCase(g1);
                });
                break;
            default:
                logger.warn("Unknown sort option: {}", sortOption);
                return;
        }
        
        // Refresh the filtered list to apply sorting
        filterSeries();
    }

    /**
     * Shows the advanced search dialog with multiple genre selection and improved visibility.
     */
    @FXML
    private void showAdvancedSearchDialog() {
        try {
            // Create a custom dialog
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Advanced Search");
            dialog.setHeaderText("Search for series with advanced filters");

            // Set the button types
            ButtonType searchButtonType = new ButtonType("Search", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

            // Create the search form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            // Title field
            TextField titleField = new TextField();
            titleField.setPromptText("Title");
            titleField.setStyle("-fx-pref-width: 300px; -fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #666;");

            // Year range fields
            Spinner<Integer> yearFromSpinner = new Spinner<>(1900, 2100, 1990);
            Spinner<Integer> yearToSpinner = new Spinner<>(1900, 2100, Calendar.getInstance().get(Calendar.YEAR));
            yearFromSpinner.setStyle("-fx-pref-width: 100px; -fx-padding: 5;");
            yearToSpinner.setStyle("-fx-pref-width: 100px; -fx-padding: 5;");

            // Genre selection (checkboxes in a scrollable pane)
            VBox genreBox = new VBox(5);
            genreBox.setStyle("-fx-padding: 5; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-border-width: 1;");
            ScrollPane genreScroll = new ScrollPane(genreBox);
            genreScroll.setFitToWidth(true);
            genreScroll.setPrefHeight(150);
            genreScroll.setStyle("-fx-background: #f5f5f5; -fx-border-color: #ddd;");

            // Add checkboxes for each genre
            Map<CheckBox, Genre> genreCheckBoxes = new HashMap<>();
            for (Genre genre : Genre.values()) {
                CheckBox checkBox = new CheckBox(genre.name());
                checkBox.setStyle("-fx-text-fill: #333; -fx-font-size: 13;");
                genreBox.getChildren().add(checkBox);
                genreCheckBoxes.put(checkBox, genre);
            }

            // Rating slider
            Slider ratingSlider = new Slider(0, 10, 0);
            ratingSlider.setShowTickMarks(true);
            ratingSlider.setShowTickLabels(true);
            ratingSlider.setMajorTickUnit(2);
            ratingSlider.setMinorTickCount(1);
            ratingSlider.setSnapToTicks(true);
            ratingSlider.setStyle("-fx-padding: 5 0 0 0;");

            // Sort by combo box with improved visibility
            ComboBox<String> sortByCombo = new ComboBox<>();
            sortByCombo.getItems().addAll("Relevance", "Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (Highest)", "Rating (Lowest)");
            sortByCombo.setValue("Relevance");
            sortByCombo.setStyle(
                    "-fx-pref-width: 200px; " +
                            "-fx-background-color: white; " +
                            "-fx-text-fill: #333; " +
                            "-fx-font-size: 13px; " +
                            "-fx-border-color: #666; " +
                            "-fx-border-radius: 5; " +
                            "-fx-padding: 8;"
            );

            // Add components to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0, 2, 1);

            grid.add(new Label("Year Range:"), 0, 1);
            HBox yearBox = new HBox(10, yearFromSpinner, new Label("to"), yearToSpinner);
            yearBox.setAlignment(Pos.CENTER_LEFT);
            grid.add(yearBox, 1, 1, 2, 1);

            grid.add(new Label("Genres:"), 0, 2);
            grid.add(genreScroll, 1, 2, 2, 1);

            grid.add(new Label("Minimum Rating:"), 0, 3);
            grid.add(ratingSlider, 1, 3, 2, 1);

            grid.add(new Label("Sort By:"), 0, 4);
            grid.add(sortByCombo, 1, 4, 2, 1);

            // Style the dialog pane
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setStyle(
                    "-fx-background-color: #f9f9f9; " +
                            "-fx-padding: 10;"
            );

            // Style the buttons
            Node searchButton = dialog.getDialogPane().lookupButton(searchButtonType);
            searchButton.setStyle(
                    "-fx-background-color: #4CAF50; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8 16; " +
                            "-fx-background-radius: 5;"
            );

            Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setStyle(
                    "-fx-background-color: #f44336; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8 16; " +
                            "-fx-background-radius: 5;"
            );

            // Convert the result to a map when the search button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == searchButtonType) {
                    Map<String, Object> searchParams = new HashMap<>();
                    searchParams.put("title", titleField.getText().trim());
                    searchParams.put("yearFrom", yearFromSpinner.getValue());
                    searchParams.put("yearTo", yearToSpinner.getValue());

                    // Get selected genres
                    List<Genre> selectedGenres = genreCheckBoxes.entrySet().stream()
                            .filter(entry -> entry.getKey().isSelected())
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toList());
                    searchParams.put("genres", selectedGenres);

                    searchParams.put("minRating", ratingSlider.getValue());
                    searchParams.put("sortBy", sortByCombo.getValue());

                    return searchParams;
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(this::performAdvancedSearch);

        } catch (Exception e) {
            logger.error("Error showing advanced search dialog", e);
            showError("Error", "Failed to show advanced search dialog: " + e.getMessage());
        }
    }

    /**
     * Performs the advanced search with the given parameters.
     *
     * @param searchParams The search parameters
     */
    private void performAdvancedSearch(Map<String, Object> searchParams) {
        try {
            String title = (String) searchParams.get("title");
            int yearFrom = (int) searchParams.get("yearFrom");
            int yearTo = (int) searchParams.get("yearTo");
            @SuppressWarnings("unchecked")
            List<Genre> genres = (List<Genre>) searchParams.get("genres");
            double minRating = (double) searchParams.get("minRating");
            String sortBy = (String) searchParams.get("sortBy");

            // Apply filters
            List<Series> filtered = allSeries.stream()
                    .filter(series -> title.isEmpty() || series.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .filter(series -> series.getStartYear() >= yearFrom && series.getStartYear() <= yearTo)
                    .filter(series -> {
                        if (genres.isEmpty()) return true;
                        return genres.stream().anyMatch(genre -> series.getGenres().contains(genre));
                    })
                    .filter(series -> series.getRating() >= minRating)
                    .collect(Collectors.toList());

            // Apply sorting
            Comparator<Series> comparator = getSortComparator(sortBy);
            if (comparator != null) {
                filtered.sort(comparator);
            }

            // Update the table
            filteredSeries.setAll(filtered);

            // Show result count
            statusLabel.setText(String.format("Found %d series matching your criteria", filtered.size()));

        } catch (Exception e) {
            logger.error("Error performing advanced search", e);
            showError("Search Error", "Failed to perform search: " + e.getMessage());
        }
    }

    /**
     * Returns a comparator based on the sort option.
     */
    private Comparator<Series> getSortComparator(String sortOption) {
        if (sortOption == null) return null;

        return switch (sortOption) {
            case "Title (A-Z)" -> Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "Title (Z-A)" -> Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Year (Newest)" -> Comparator.comparingInt(Series::getStartYear).reversed();
            case "Year (Oldest)" -> Comparator.comparingInt(Series::getStartYear);
            case "Rating (Highest)" -> Comparator.comparingDouble(Series::getRating).reversed();
            case "Rating (Lowest)" -> Comparator.comparingDouble(Series::getRating);
            default -> // Relevance or unknown
                    null;
        };
    }

    /*
     * Handle add series action
     */
    @FXML
    private void handleAddSeries(ActionEvent event) {
        if (seriesService == null) {
            logger.error("SeriesService is not initialized");
            showError("Error", "Series service is not available");
            return;
        }

        try {
            // Create a new series with default values
            Series newSeries = new Series("New Series");

            // Show the edit dialog for the new series
            if (showSeriesEditDialog(newSeries)) {
                // Save the new series using the instance method
                seriesService.save(newSeries);
                loadSeries();
                showSuccess("Success", "Series added successfully!");
            }
        } catch (Exception e) {
            logger.error("Error adding series", e);
            showError("Error", "Failed to add series: " + e.getMessage());
        }
    }


    @FXML
    private void handleRateSeries(ActionEvent event) {
        Series selected = seriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a series to rate.");
            return;
        }

        // Create a dialog to get the rating
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rate Series");
        dialog.setHeaderText(String.format("Rate %s (%d)\nCurrent Rating: %.1f/10",
                selected.getTitle(), 
                selected.getStartYear(),
                selected.getImdbRating()));
                
        // Add message for guest users
        String contentText = "Enter your rating (0.0 - 10.0):";
        try {
            int currentUserId = getCurrentUserId();
            if (currentUserId <= 0) {
                contentText += "\n\nYou're rating as a guest. Sign in to save your ratings across devices.";
            }
        } catch (Exception e) {
            // User is not authenticated, which is fine
            contentText += "\n\nYou're rating as a guest. Sign in to save your ratings across devices.";
        }
        
        dialog.setContentText(contentText);

        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String ratingStr = result.get().trim();
                if (ratingStr.isEmpty()) {
                    showAlert("Invalid Input", "Please enter a valid rating");
                    return;
                }
                
                // Parse and validate the rating
                double rating = Double.parseDouble(ratingStr);
                if (rating < 0.0 || rating > 10.0) {
                    showAlert("Invalid Rating", "Please enter a rating between 0.0 and 10.0");
                    return;
                }

                // Round to one decimal place
                rating = Math.round(rating * 10) / 10.0;

                // Get the current user ID, default to guest if not authenticated
                int currentUserId = -1; // Default guest user ID
                try {
                    currentUserId = getCurrentUserId();
                } catch (Exception e) {
                    logger.debug("User not authenticated, using guest ID");
                }

                // Save the user's rating (stored as 0-20 internally)
                int internalRating = (int) Math.round(rating * 2);
                selected.setUserRating(currentUserId, internalRating);

                // Update the series in the service
                seriesService.update(selected);

                // Show success message
                showSuccess("Success", 
                    String.format("You rated %s: %.1f/10%nNew average rating: %.1f/10",
                        selected.getTitle(),
                        rating,
                        selected.getImdbRating()));

                // Show only the user's rated series
                showMyRatedSeries();

            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number between 0 and 10");
            } catch (Exception e) {
                logger.error("Error rating series: {}", e.getMessage(), e);
                showAlert("Error", "Failed to save rating: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            currentUserId = getCurrentUserId();
            // Reload series data
            loadSeries();
            
            // Update UI
            if (seriesTable != null) {
                seriesTable.refresh();
            }
            
            showSuccess("Success", "Series list has been refreshed.");
            logger.info("Successfully refreshed series list");
            
        } catch (Exception e) {
            String errorMsg = "Error refreshing series: " + e.getMessage();
            logger.error(errorMsg, e);
            showError("Refresh Error", "Failed to refresh series. Please try again.");
        }
    }
    
    /**
     * Shows all series that have been rated by any user
     */
    @FXML
    private void showMyRatedSeries() {
        try {
            List<Series> ratedSeries;
            
            try {
                int currentUserId = getCurrentUserId();
                // If user is logged in, show only their rated series
                ratedSeries = allSeries.stream()
                    .filter(series -> series.getUserRatings() != null && !series.getUserRatings().isEmpty())
                    .filter(series -> series.getUserRatings().containsKey(currentUserId))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                // If not logged in, show all rated series
                ratedSeries = allSeries.stream()
                    .filter(series -> series.getUserRatings() != null && !series.getUserRatings().isEmpty())
                    .collect(Collectors.toList());
            }
                
            if (ratedSeries.isEmpty()) {
                showInformation("No Rated Series", "No series have been rated yet.");
                return;
            }
            
            // Update the filtered series list and refresh the table
            filteredSeries.setAll(ratedSeries);
            seriesTable.setItems(filteredSeries);
            updateItemCount();
            
        } catch (Exception e) {
            logger.error("Error loading rated series: {}", e.getMessage(), e);
            showError("Error", "Failed to load rated series.");
        }
    }

    /**
     * Updates the results count label with the current number of items in the table
     */
    private void updateItemCount() {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText("Results: " + seriesTable.getItems().size());
            logger.info("Updated item count to {}", seriesTable.getItems().size());
        }
    }

    // Status label for showing status messages
    @FXML
    private Label statusLabel;
    
    /**
     * Shows an information dialog with the given title and message
     * @param title The title of the dialog
     * @param message The message to display
     */
    private void showInformation(String title, String message) {
        showAlert(title, message);
    }

    /**
     * Shows a dialog for editing series details.
     *
     * @param series The series to edit
     * @return true if the user clicked OK, false otherwise
     */
    private boolean showSeriesEditDialog(Series series) {
        try {
            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(series.getTitle() != null && !series.getTitle().isEmpty() ? "Edit Series" : "Add New Series");
            dialog.setHeaderText("Series Details");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form with scroll pane for many fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            // Title
            TextField titleField = new TextField(series.getTitle());
            titleField.setPromptText("Title");
            titleField.setMinWidth(300);

            // Start Year
            Spinner<Integer> startYearSpinner = new Spinner<>(1900, 2100,
                    series.getReleaseYear() > 0 ? series.getReleaseYear() : Calendar.getInstance().get(Calendar.YEAR));
            startYearSpinner.setEditable(true);

            // End Year
            Spinner<Integer> endYearSpinner = new Spinner<>(1900, 2100,
                    series.getEndYear() > 0 ? series.getEndYear() : Calendar.getInstance().get(Calendar.YEAR));
            endYearSpinner.setEditable(true);

            // Genre Selection (multi-select)
            VBox genreBox = new VBox(5);
            genreBox.setStyle("-fx-padding: 5; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");
            ScrollPane genreScroll = new ScrollPane(genreBox);
            genreScroll.setFitToWidth(true);
            genreScroll.setPrefHeight(100);

            // Add checkboxes for each genre
            Map<CheckBox, Genre> genreCheckBoxes = new HashMap<>();
            Set<Genre> selectedGenres = new HashSet<>(series.getGenres());

            for (Genre genre : Genre.values()) {
                CheckBox checkBox = new CheckBox(genre.name());
                checkBox.setSelected(selectedGenres.contains(genre));
                checkBox.setStyle("-fx-text-fill: #333; -fx-font-size: 13;");
                genreBox.getChildren().add(checkBox);
                genreCheckBoxes.put(checkBox, genre);
            }

            // Rating
            Spinner<Double> ratingSpinner = new Spinner<>(0.0, 10.0,
                    series.getImdbRating() != null ? series.getImdbRating() : 0.0, 0.1);
            ratingSpinner.setEditable(true);

            // Director
            TextField directorField = new TextField(series.getDirector() != null ? series.getDirector() : "");
            directorField.setPromptText("Director");

            // Cast (comma-separated)
            TextArea castArea = new TextArea();
            castArea.setPromptText("Enter cast members, one per line");
            castArea.setPrefRowCount(3);
            if (series.getActors() != null && !series.getActors().isEmpty()) {
                StringBuilder castText = new StringBuilder();
                for (Actor actor : series.getActors()) {
                    if (actor != null && actor.getFullName() != null) {
                        if (!castText.isEmpty()) {
                            castText.append("\n");
                        }
                        castText.append(actor.getFullName());
                    }
                }
                castArea.setText(castText.toString());
            }

            // Add fields to grid
            int row = 0;
            grid.add(new Label("Title*:"), 0, row);
            grid.add(titleField, 1, row++);

            grid.add(new Label("Start Year*:"), 0, row);
            grid.add(startYearSpinner, 1, row++);

            grid.add(new Label("End Year:"), 0, row);
            grid.add(endYearSpinner, 1, row++);

            grid.add(new Label("Genres*:"), 0, row);
            grid.add(genreScroll, 1, row++);

            grid.add(new Label("Rating (0-10):"), 0, row);
            grid.add(ratingSpinner, 1, row++);

            grid.add(new Label("Director:"), 0, row);
            grid.add(directorField, 1, row++);

            grid.add(new Label("Cast (one per line):"), 0, row);
            grid.add(castArea, 1, row++);

            // Make the dialog resizable
            ScrollPane scrollPane = new ScrollPane(grid);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.setResizable(true);
            dialog.getDialogPane().setPrefSize(500, 500);

            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);

            // Convert the result to a series when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        // Basic validation
                        if (titleField.getText().trim().isEmpty()) {
                            showError("Error", "Title is required");
                            return null;
                        }

                        // Update series with new values
                        series.setTitle(titleField.getText().trim());

                        // Set the years
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, startYearSpinner.getValue());
                        series.setYear(cal.getTime());
                        series.setEndYear(endYearSpinner.getValue());

                        // Update genres
                        List<Genre> newGenres = genreCheckBoxes.entrySet().stream()
                                .filter(entry -> entry.getKey().isSelected())
                                .map(Map.Entry::getValue)
                                .toList();
                        series.getGenres().clear();
                        series.getGenres().addAll(newGenres);

                        // Update rating
                        series.setImdbRating(ratingSpinner.getValue());

                        // Update director
                        series.setDirector(directorField.getText().trim());

                        // Update cast
                        if (!castArea.getText().trim().isEmpty()) {
                            List<Actor> actors = new ArrayList<>();
                            for (String nameLine : castArea.getText().split("\\n")) {
                                String name = nameLine.trim();
                                if (!name.isEmpty()) {
                                    // Create a new Actor with default values where required
                                    // Using current date, 'U' for unknown gender, and null ethnicity as defaults
                                    Actor actor = new Actor(
                                            name, // First name
                                            "",   // Last name (empty since we don't have this info)
                                            java.time.LocalDate.now(), // Default to current date
                                            'U',  // 'U' for unknown gender
                                            (Ethnicity) null // No ethnicity specified
                                    );
                                    actors.add(actor);
                                }
                            }
                            series.setActors(actors);
                        } else {
                            series.setActors(new ArrayList<>());
                        }

                        return saveButtonType;
                    } catch (Exception e) {
                        logger.error("Error updating series", e);
                        showError("Error", "Failed to update series: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<ButtonType> result = dialog.showAndWait();
            return result.isPresent() && result.get() == saveButtonType;
        } catch (Exception e) {
            logger.error("Error showing series edit dialog", e);
            showError("Error", "Failed to show series editor: " + e.getMessage());
            return false;
        }
    }


    /**
     * Gets the current user's ID.
     * This is a placeholder - you'll need to implement this based on your authentication system.
     *
     * @return The current user's ID
     */
    private int getCurrentUserId() {
        return currentUserId;
    }


}
