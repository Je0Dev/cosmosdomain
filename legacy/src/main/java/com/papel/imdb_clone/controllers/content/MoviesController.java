package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.util.UIUtils;
import com.papel.imdb_clone.model.content.Movie;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.util.Duration;

/**
 * Controller for managing movies in the application.
 * Handles all movie-related operations including listing, adding, editing, and deleting movies.
 */
public class MoviesController extends BaseController {

    @FXML
    public Label resultsCountLabel;
    @FXML
    public Label itemCountLabel;
    @FXML
    private MoviesService moviesService;

    private Movie selected;
    private final MoviesDialogHelper dialogHelper = new MoviesDialogHelper();
    private final MoviesLogicHelper logicHelper = new MoviesLogicHelper();

    public MoviesController() {
        super();
        NavigationService navigationService = NavigationService.getInstance();
    }


    /**
     * Handles the edit movie button click event.
     * Opens a dialog to edit the selected movie.
     * 
     * @param event The action event
     */
    @FXML
    private void handleEditMovie(ActionEvent event) {
        try {
            // Get the selected movie from the table
            Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
            logger.info("Selected movie for editing: {}", selectedMovie);
            
            if (selectedMovie != null) {
                // Show the edit dialog for the selected movie
                if (showMovieEditDialog(selectedMovie)) {
                    try {
                        // Save changes if user clicked OK
                        moviesService.update(selectedMovie);
                        loadMovies(); // Refresh the table
                        showSuccess("Success", "Movie updated successfully!");
                    } catch (Exception e) {
                        logger.error("Error updating movie", e);
                        showError("Error", "Failed to update movie: " + e.getMessage());
                    }
                }
            } else {
                showAlert("No Selection", "Please select a movie to edit.");
            }
        } catch (Exception e) {
            logger.error("Error in handleEditMovie", e);
            showError("Error", "An error occurred while trying to edit the movie: " + e.getMessage());
        }
    }

    /**
     * Deletes the selected movie from the table.
     * @param event The action event
     */
    @FXML
    private void handleDeleteMovie(ActionEvent event) {
        try {
            Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
            logger.info("Selected movie for deletion: {}", selectedMovie);
            
            if (selectedMovie == null) {
                showAlert("No Selection", "Please select a movie to delete.");
                return;
            }

            // Show confirmation dialog
            boolean confirmDelete = showConfirmationDialog(
                "Confirm Deletion",
                String.format("Are you sure you want to delete '%s'?\nThis action cannot be undone.", 
                    selectedMovie.getTitle())
            );

            if (!confirmDelete) {
                logger.debug("User cancelled movie deletion");
                return;
            }

            try {
                // Call the service to delete the movie
                boolean deleted = moviesService.delete(selectedMovie.getId());
                
                if (deleted) {
                    // Remove from the table and update UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            allMovies.remove(selectedMovie);
                            filteredMovies.remove(selectedMovie);
                            movieTable.refresh();
                            updateItemCount();
                            showSuccess("Success", String.format("Movie '%s' was deleted successfully.", 
                                selectedMovie.getTitle()));
                            logger.info("Successfully deleted movie: {}", selectedMovie.getTitle());
                        } catch (Exception e) {
                            logger.error("Error updating UI after movie deletion", e);
                            showError("Error", "An error occurred while updating the UI. Please refresh the view.");
                        }
                    });
                } else {
                    throw new IllegalStateException("Failed to delete movie from the database");
                }
            } catch (Exception e) {
                logger.error("Error deleting movie: {}", e.getMessage(), e);
                showError("Deletion Failed", "Failed to delete the movie. Please try again later.");
            }
        } catch (Exception e) {
            logger.error("Unexpected error in handleDeleteMovie: {}", e.getMessage(), e);
            showError("Error", "An unexpected error occurred while processing your request.");
        }
    }

    //updates the item count label in movies v
    private void updateItemCount() {
        itemCountLabel.setText(String.format("Total Movies: %d", movieTable.getItems().size()));
        itemCountLabel.setVisible(true);
        itemCountLabel.setStyle("-fx-text-fill: #2e7d32;");
        itemCountLabel.setOpacity(1);
    }

    /**
     * Handles the rate movie button click event.
     * Opens a dialog to allow the user to rate the selected movie.
     *
     * @param event The action event that triggered this method
     */
    @FXML
    private void handleRateMovie(ActionEvent event) {
        try {
            // Get the selected movie
            Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
            if (selectedMovie == null) {
                showWarning("No Selection", "Please select a movie to rate.");
                return;
            }

            // Use a default user ID for unauthenticated users
            int currentUserId = -1; // Default user ID for unauthenticated users
            try {
                // Try to get the current user ID, but don't fail if not authenticated
                currentUserId = getCurrentUserId();
            } catch (Exception e) {
                // User is not authenticated, use default ID
                logger.debug("User not authenticated, using default ID");
            }

            // Build the message with movie info and rating
            String message = String.format("'%s' (%d)\n\n" +
                         "Average Rating: %.1f/10\n\n", 
                         selectedMovie.getTitle(), 
                         selectedMovie.getReleaseYear(),
                         selectedMovie.getImdbRating());

            // Show rating dialog for all users, regardless of authentication status
            if (currentUserId <= 0) {
                message += "You're rating as a guest. Sign in to save your ratings across devices.";
            }

            // Create a dialog for rating
            Dialog<Double> dialog = new Dialog<>();
            dialog.setTitle("Rate Movie");
            dialog.setHeaderText(String.format("Rate '%s' (0-10)", selectedMovie.getTitle()));

            // Set the button types
            ButtonType rateButtonType = new ButtonType("Rate", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(rateButtonType, ButtonType.CANCEL);

            // Create the rating input
            TextField ratingField = new TextField();
            ratingField.setPromptText("0-10");
            
            // Add input validation
            ratingField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*\\.?\\d*")) {
                    ratingField.setText(oldValue);
                }
            });

            dialog.getDialogPane().setContent(new VBox(10, 
                new Label(message),
                new Label("Your Rating (0-10):"),
                ratingField
            ));

            // Request focus on the rating field by default
            Platform.runLater(ratingField::requestFocus);

            // Convert the result to a rating when the rate button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == rateButtonType) {
                    try {
                        String ratingText = ratingField.getText().trim();
                        if (ratingText.isEmpty()) {
                            showWarning("Input Required", "Please enter a rating between 0 and 10");
                            return null;
                        }
                        
                        double rating = Double.parseDouble(ratingText);
                        if (rating < 0 || rating > 10) {
                            throw new IllegalArgumentException("Rating must be between 0 and 10");
                        }
                        return rating;
                    } catch (NumberFormatException e) {
                        showWarning("Invalid Input", "Please enter a valid number between 0 and 10");
                        return null;
                    } catch (IllegalArgumentException e) {
                        showWarning("Invalid Rating", e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Create a final copy of currentUserId for use in lambda
            final int finalCurrentUserId = currentUserId;
            
            // Show the dialog and process the result
            Optional<Double> result = dialog.showAndWait();
            result.ifPresent(rating -> {
                try {
                    // Update the movie rating - ensure it's between 0 and 10
                    int ratingValue = (int) Math.round(rating * 2);
                    ratingValue = Math.max(0, Math.min(10, ratingValue)); // Clamp between 0 and 10
                    selectedMovie.setUserRating(finalCurrentUserId, ratingValue);
                    Movie updatedMovie = moviesService.update(selectedMovie);
                    
                    // Update the UI
                    Platform.runLater(() -> {
                        int index = allMovies.indexOf(selectedMovie);
                        if (index >= 0) {
                            allMovies.set(index, updatedMovie);
                        }
                        
                        index = filteredMovies.indexOf(selectedMovie);
                        if (index >= 0) {
                            filteredMovies.set(index, updatedMovie);
                        }
                        
                        movieTable.refresh();
                        showSuccess("Success", 
                            String.format("You rated '%s': %.1f/10\nNew average: %.1f/10",
                                updatedMovie.getTitle(),
                                rating,
                                updatedMovie.getImdbRating()));
                        showMyRatedMovies();
                    });
                } catch (Exception e) {
                    logger.error("Error saving rating: {}", e.getMessage(), e);
                    showError("Error", "Failed to save rating. Please try again.");
                }
            });

        } catch (Exception e) {
            logger.error("Error in handleRateMovie: {}", e.getMessage(), e);
            showError("Error", "An error occurred while processing your request.");
        }
    }

    /**
     * Gets the current user's ID.
     * 
     * @return the current user's ID
     * @throws IllegalStateException if no user is currently authenticated or user ID is invalid
     */
    private int getCurrentUserId() {
        if (currentUserId <= 0) {
            String errorMsg = "No valid user is currently authenticated. User ID: " + currentUserId;
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        return currentUserId;
    }

    // UI Components
    @FXML private Label statusLabel;
    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, String> movieTitleColumn;
    @FXML private TableColumn<Movie, String> movieYearColumn;
    @FXML private TableColumn<Movie, Integer> movieDurationColumn;
    @FXML private TableColumn<Movie, String> movieGenreColumn;
    @FXML private TableColumn<Movie, String> movieDirectorColumn;
    @FXML private TableColumn<Movie, String> movieCastColumn;
    @FXML private TableColumn<Movie, Double> movieRatingColumn;
    @FXML private TextField movieSearchField;
    @FXML private ComboBox<String> movieSortBy;
    private Map<String, Object> data;

    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/base/home-view.fxml",
                    data, (Stage) movieTable.getScene().getWindow(),
                "Home");
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            UIUtils.showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }

    // Data and state
    private final ObservableList<Movie> allMovies = FXCollections.observableArrayList();
    private final ObservableList<Movie> filteredMovies = FXCollections.observableArrayList();
    private final ObjectProperty<Movie> selectedMovie = new SimpleObjectProperty<>();
    private int currentUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Required by Initializable
    }

    @Override
    public void initializeController(int currentUserId) throws Exception {
        try {
            this.currentUserId = currentUserId;
            // Initialize movies service
            this.moviesService = MoviesService.getInstance();

            // Set up the table columns first
            setupTableColumns();

            // Bind the table to the filtered movies list
            movieTable.setItems(filteredMovies);

            // Set up selection listener before loading data
            movieTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectedMovie.set(newSelection)
            );

            // Set up search and sort handlers
            setupSearchHandlers();
            setupSortHandlers();

            // Load data last, after UI is fully set up
            loadMovies();

        } catch (Exception e) {
            logger.error("Error initializing MoviesController", e);
            showError("Initialization Error", "Failed to initialize movie controller: " + e.getMessage());
            throw e;
        }
    }



// Setter for content service (for dependency injection)
public void setContentService(MoviesService moviesService) {
    this.moviesService = moviesService;
}

//setup table columns
private void setupTableColumns() {
    // Set up the movie title column
    movieTitleColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleStringProperty(movie != null ? movie.getTitle() : "");
    });

    // Set up the year column
    movieYearColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        if (movie != null && movie.getReleaseDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(movie.getReleaseDate());
            return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
        }
        return new SimpleStringProperty("");
    });
    
    // Set up the duration column (in minutes)
    movieDurationColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleObjectProperty<Integer>(movie != null ? (Integer) movie.getDuration() : 0);
    });
    
    // Set up the genre column
    movieGenreColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        if (movie != null && movie.getGenre() != null) {
            return new SimpleStringProperty(movie.getGenre().toString());
        }
        return new SimpleStringProperty("");
    });
    
    // Set up the director column
    movieDirectorColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleStringProperty(movie != null && movie.getDirector() != null ? movie.getDirector() : "");
    });
    
    // Set up the cast column
    movieCastColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        if (movie != null && movie.getActors() != null && !movie.getActors().isEmpty()) {
            return new SimpleStringProperty(
                movie.getActors().stream()
                    .filter(Objects::nonNull)
                    .map(actor -> {
                        String firstName = actor.getFirstName() != null ? actor.getFirstName() : "";
                        String lastName = actor.getLastName() != null ? actor.getLastName() : "";
                        return (firstName + " " + lastName).trim();
                    })
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.joining(", "))
            );
        }
        return new SimpleStringProperty("");
    });
    
    // Set up the rating column
    movieRatingColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleObjectProperty<>(movie != null ? movie.getImdbRating() : 0.0);
        });
        
        // Set up year column
        movieYearColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up genre column
        movieGenreColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            if (movie == null) {
                return new SimpleStringProperty("");
            }
            String genres = movie.getGenres().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(genres);
        });

        //Set up genre column
        movieGenreColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up director column
        movieDirectorColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            String directorName = "";
            if (movie != null) {
                directorName = movie.getDirector();
                if (directorName == null || directorName.trim().isEmpty()) {
                    directorName = "Unknown Director";
                }
            }
            return new SimpleStringProperty(directorName);
        });
        //Set up director column
        movieDirectorColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up cast column with enhanced display
        movieCastColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            if (movie == null || movie.getActors() == null || movie.getActors().isEmpty()) {
                return new SimpleStringProperty("No cast information available");
            }
            
            // Get all actors
            List<Actor> allActors = new ArrayList<>(movie.getActors());

            
            // Create a string with up to 5 actors for the main display
            int maxActorsToShow = Math.min(allActors.size(), 5);
            String actorsDisplay = allActors.stream()
                    .limit(maxActorsToShow)
                    .map(actor -> {
                        String role = (actor.getRole() != null && !actor.getRole().trim().isEmpty())
                                ? " (" + actor.getRole().trim() + ")"
                                : "";
                        return actor.getFullName() + role;
                    })
                    .collect(Collectors.joining(", "));

            // Add "+X more" indicator if there are more actors
            if (allActors.size() > maxActorsToShow) {
                actorsDisplay += " +" + (allActors.size() - maxActorsToShow) + " more";
            }
            
            return new SimpleStringProperty(actorsDisplay);
        });

        // Set up cast column cell factory with tooltips
        movieCastColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            private final Tooltip tooltip = new Tooltip();
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText("");
                    setTooltip(null);
                    setStyle("");
                    return;
                }
                
                // Get the movie for this row
                Movie movie = getTableView().getItems().get(getIndex());
                if (movie == null || movie.getActors() == null || movie.getActors().isEmpty()) {
                    setText("No cast");
                    setTooltip(null);
                    return;
                }
                
                // Set the display text with bold style
                setText(item);
                setStyle("-fx-font-weight: bold; -fx-text-fill: #e0e0e0;");
                
                // Create a detailed tooltip with all actors and their roles
                String tooltipText = movie.getActors().stream()
                        .map(actor -> {
                            String role = (actor.getRole() != null && !actor.getRole().trim().isEmpty())
                                    ? " as " + actor.getRole()
                                    : "";
                            return "• " + actor.getFullName() + role;
                        })
                        .collect(Collectors.joining("\n"));
                
                tooltip.setText(tooltipText);
                tooltip.setStyle(
                    "-fx-font-size: 12px;" +
                    "-fx-padding: 5px;" +
                    "-fx-background-color: #2a2a2a;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-border-color: #444;"
                );
                tooltip.setMaxWidth(300);
                tooltip.setWrapText(true);
                
                // Only show tooltip if there are actors to show
                setTooltip(tooltip);
                
                // Style the cell with bold text
                setStyle(
                    "-fx-alignment: CENTER_LEFT; " +
                    "-fx-padding: 8 12; " +
                    "-fx-font-size: 13px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #e0e0e0;"
                );
                setWrapText(true);
            }
        });

        // Set up rating column
        movieRatingColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            return new SimpleObjectProperty<>(movie != null ? movie.getImdbRating() : 0.0);
        });
        //Set up rating column cell factory which means the rating will be displayed in the center of the cell
        movieRatingColumn.setCellFactory(col -> new TableCell<Movie, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("");
                } else {
                    setText(String.format("%.1f", item));
                }
                setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
            }
        });
    }

    private PauseTransition searchDebounce; // for real time search.Debounce means delay the search for a certain amount of time
    private static final int SEARCH_DELAY_MS = 300; // 300ms delay for debouncing

    private void setupSearchHandlers() {
        // Initialize debounce timer
        searchDebounce = new PauseTransition(Duration.millis(SEARCH_DELAY_MS));
        searchDebounce.setOnFinished(event -> {
            filterMovies();
            // Remove loading indicator after search completes
            movieSearchField.setStyle("-fx-prompt-text-fill: derive(-fx-text-inner-color, -30%);");
        });

        // Add text change listener with debounce
        movieSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Show loading indicator while typing
            movieSearchField.setStyle("-fx-prompt-text-fill: #666666; -fx-font-style: italic;");
            
            // Reset and restart the debounce timer
            searchDebounce.stop();
            searchDebounce.playFromStart();
        });

        // Clear search when the 'x' is clicked
        movieSearchField.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY &&
                event.getClickCount() == 1 && 
                movieSearchField.getText().isEmpty()) {
                clearSearch();
            }
        });
    }

    private void clearSearch() {
        movieSearchField.clear();
        filterMovies();
    }

    //Setup sort handlers
    private void setupSortHandlers() {
        // Initialize sort options
        movieSortBy.getItems().addAll(
            "Title (A-Z)",
            "Title (Z-A)",
            "Year (Newest First)",
            "Year (Oldest First)",
            "Rating (High to Low)",
            "Rating (Low to High)"
        );

        // Set default sort option
        movieSortBy.getSelectionModel().selectFirst();

        // Add listener for sort selection changes
        movieSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sortMovieTable(newVal);
            }
        });
    }
    //Load movies from the service
    private void loadMovies() {
        if (moviesService == null) {
            logger.error("ContentService is not initialized");
            Platform.runLater(() ->
                statusLabel.setText("Error: Content service not initialized")
            );
            return;
        }

        logger.info("Starting to load movies...");
        logger.debug("ContentService type: {}", moviesService.getContentType());

        try {
            List<Movie> movies = moviesService.getAll();
            logger.info("Retrieved {} movies from service", movies.size());

            if (movies.isEmpty()) {
                logger.warn("No movies found in the database");
                Platform.runLater(() ->
                    statusLabel.setText("No movies found")
                );
                return;
            }

            // Log first few movies for debugging
            int logCount = Math.min(3, movies.size());
            for (int i = 0; i < logCount; i++) {
                Movie movie = movies.get(i);
                logger.debug("Movie #{}: {} ({}), Genres: {}, Director: {}",
                    i + 1,
                    movie.getTitle(),
                    movie.getYear(),
                    movie.getGenres().stream().map(Enum::name).collect(Collectors.joining(", ")),
                    movie.getDirector()
                );
            }

            Platform.runLater(() -> {
                try {
                    // Clear existing data
                    allMovies.clear();
                    
                    // Update results count
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", movies.size()));
                    } else {
                        logger.warn("resultsCountLabel is not initialized");
                    }
                    
                    // Use a Set to filter out duplicates based on title, year, and director
                    Set<String> uniqueMovieKeys = new HashSet<>();
                    List<Movie> uniqueMovies = new ArrayList<>();
                    
                    for (Movie movie : movies) {
                        String key = movie.getTitle().toLowerCase() + "_" + movie.getYear() + "_" + 
                                   (movie.getDirector() != null ? movie.getDirector().toLowerCase() : "");
                        
                        if (uniqueMovieKeys.add(key)) { // add returns true if the key was not already in the set
                            uniqueMovies.add(movie);
                        } else {
                            logger.debug("Skipping duplicate movie: {} ({})", movie.getTitle(), movie.getYear());
                        }
                    }
                    
                    // Add only unique movies to the observable list
                    allMovies.addAll(uniqueMovies);
                    logger.info("Added {} unique movies to allMovies list ({} duplicates filtered out)", 
                              uniqueMovies.size(), movies.size() - uniqueMovies.size());
                    
                    // Update the filtered list and UI
                    filterMovies();
                    
                    // Update results count
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", uniqueMovies.size()));
                    }
                    
                    statusLabel.setText(String.format("Loaded %d unique movies", uniqueMovies.size()));
                    logger.info("Successfully loaded and displayed {} unique movies", uniqueMovies.size());
                } catch (Exception e) {
                    logger.error("Error in Platform.runLater", e);
                    showError("Error", "Failed to update UI: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error loading movies", e);
            Platform.runLater(() ->
                showError("Error", "Failed to load movies: " + e.getMessage())
            );
        }
    }

    //filter movies based on search text
    private void filterMovies() {
        String searchText = movieSearchField.getText().trim().toLowerCase();
        
        // If search is empty, show all movies
        if (searchText.isEmpty()) {
            filteredMovies.setAll(allMovies);
            updateItemCount();
            statusLabel.setVisible(false);
            if (resultsCountLabel != null) {
                resultsCountLabel.setText(String.format("Results: %d", allMovies.size()));
            }
            return;
        }

        // Show loading indicator
        Platform.runLater(() -> {
            statusLabel.setText("Searching...");
            statusLabel.setVisible(true);
        });

        // Run search in background thread
        Task<Void> searchTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    // Filter movies based on search criteria
                    List<Movie> results = allMovies.stream()
                        .filter(movie -> {
                            // Check title match
                            boolean titleMatch = movie.getTitle() != null && 
                                movie.getTitle().toLowerCase().contains(searchText);
                            
                            // Check director match
                            boolean directorMatch = movie.getDirector() != null && 
                                movie.getDirector().toLowerCase().contains(searchText);
                            
                            // Check genre match
                            boolean genreMatch = movie.getGenre() != null && 
                                movie.getGenre().name().toLowerCase().contains(searchText);
                            
                            // Check cast members
                            boolean castMatch = movie.getActors() != null && 
                                movie.getActors().stream()
                                    .anyMatch(actor -> 
                                        (actor.getFirstName() != null && actor.getFirstName().toLowerCase().contains(searchText)) ||
                                        (actor.getLastName() != null && actor.getLastName().toLowerCase().contains(searchText)) ||
                                        (actor.getFullName() != null && actor.getFullName().toLowerCase().contains(searchText))
                                    );
                            
                            return titleMatch || directorMatch || genreMatch || castMatch;
                        })
                        .collect(Collectors.toList());

                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        filteredMovies.setAll(results);
                        updateItemCount();
                        String resultText = String.format("Found %d movie%s", results.size(), results.size() != 1 ? "s" : "");
                        statusLabel.setText(resultText);
                        statusLabel.setStyle("-fx-text-fill: -fx-text-inner-color;");
                        statusLabel.setVisible(true);
                        
                        if (resultsCountLabel != null) {
                            resultsCountLabel.setText(String.format("Results: %d", results.size()));
                        }
                    });
                } catch (Exception e) {
                    logger.error("Error during search", e);
                    Platform.runLater(() -> {
                        statusLabel.setText("Error during search");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    });
                }
                return null;
            }
        };

        // Start the search task in a new thread
        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }

    //sort movies based on sort option
    private void sortMovieTable(String sortOption) {
        if (sortOption == null) return;

        switch (sortOption) {
            case "Title (A-Z)":
                allMovies.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Title (Z-A)":
                allMovies.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Year (Newest First)":
                allMovies.sort(Comparator.comparingInt(Movie::getYearAsInt).reversed());
                break;
            case "Year (Oldest First)":
                allMovies.sort(Comparator.comparingInt(Movie::getYearAsInt));
        break;
    case "Rating (High to Low)":
        allMovies.sort(Comparator.comparingDouble(Movie::getRating).reversed());
        break;
    case "Rating (Low to High)":
        allMovies.sort(Comparator.comparingDouble(Movie::getRating));
        break;
    default:
        logger.warn("Unknown sort option: {}", sortOption);
        return;
}
        if (selected != null) {
            if (showMovieEditDialog(selected)) {
                try {
                    // Save changes if user clicked OK
                    moviesService.update(selected);
                    loadMovies(); // Refresh the table
                    showSuccess("Success", "Movie updated successfully!");
                } catch (Exception e) {
                    logger.error("Error updating movie", e);
                    showError("Error", "Failed to update movie: " + e.getMessage());
                }
            }
        } else {
            showAlert("No Selection", "Please select a movie to edit.");
        }
    }

    
    /**
     * Handles the "Add Movie" button click.
     * Creates a new movie and opens the edit dialog for it.
     * @param event The action event
     */
    @FXML
    private void handleAddMovie(ActionEvent event) {
        try {
            // Create a new movie with default values
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2023); // Default to current year
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            
            // Create a new movie with default values
            Movie newMovie;
            newMovie = new Movie(
                "New Movie",
                Calendar.getInstance().get(Calendar.YEAR),
                Genre.DRAMA.name(),
                "Unknown Director",
                new HashMap<>(),
                0.0
            );
            newMovie.setReleaseDate(cal.getTime());
            newMovie.setActors(new ArrayList<>());
            
            // Show the edit dialog for the new movie
            if (showMovieEditDialog(newMovie)) {
                // Save the new movie
                moviesService.save(newMovie);

                // Update the filtered list and UI
                filterMovies();

                // Update results count
                if (resultsCountLabel != null) {
                    resultsCountLabel.setText(String.format("Results: %d", allMovies.size()));
                }

                // Update the table view
                movieTable.setItems(allMovies);

                // Refresh the movie list
                loadMovies();
                
                // Show success message
                statusLabel.setText("Movie added successfully!");

                // Clear the status message after 3 seconds
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> statusLabel.setText(""));
                        }
                    },
                    3000
                );

            }
        } catch (Exception e) {
            logger.error("Error adding new movie: {}", e.getMessage(), e);
            showError("Add Movie Error", "Failed to add new movie: " + e.getMessage());
        }
    }


    /**
     * Shows the edit dialog for a movie
     * @param movie the movie to be edited
     * @return true if the movie was successfully edited, false otherwise
     */
    @FXML
    private boolean showMovieEditDialog(Movie movie) {
        try {
            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Movie".equals(movie.getTitle()) ? "Add New Movie" : "Edit Movie");
            dialog.setHeaderText("Enter movie details:");
            dialog.setResizable(false);
            dialog.setDialogPane(new DialogPane());

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);


            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Create form fields
            TextField titleField = new TextField(movie.getTitle());
            TextField yearField = new TextField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            TextField directorField = new TextField(movie.getDirector() != null ? movie.getDirector() : "");
            TextField genreField = new TextField(movie.getGenres().stream().map(Enum::name).collect(Collectors.joining(", ")));
            TextField ratingField = new TextField(String.valueOf(movie.getRating()));
            String actorNames = movie.getActors().stream()
                    .<String>map(actor -> actor.getName().toString())
                    .collect(Collectors.joining(", "));
            TextField actorsField = new TextField(actorNames);

            // Add fields to grid
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

            // Add the form to the dialog
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setMinWidth(400);
            dialog.getDialogPane().setMinHeight(300);

            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);

            // Convert the result to a response
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == saveButtonType) {
                // Update the movie with the form data
                movie.setTitle(titleField.getText().trim());
                try {
                    int year = Integer.parseInt(yearField.getText().trim());
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    movie.setReleaseDate(cal.getTime());
                    movie.setRating(Double.parseDouble(ratingField.getText().trim()));
                    movie.setGenres(Arrays.stream(genreField.getText().trim().split(","))
                            .map(String::trim)
                            .map(String::toUpperCase) // Convert to uppercase before mapping to enum
                            .map(Genre::valueOf)
                            .collect(Collectors.toList()));
                    movie.setActors(Arrays.stream(actorsField.getText().trim().split(","))
                            .map(String::trim)
                            .map(Actor::new)
                            .collect(Collectors.toList()));
                } catch (NumberFormatException e) {
                    showError("Invalid Year", "Please enter a valid year number.");
                    logger.error("Error parsing year", e);
                    return false;
                }
                movie.setDirector(directorField.getText().trim());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error showing movie edit dialog", e);
            showError("Error", "Failed to show movie editor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Refreshes the movies list
     *
     * @param actionEvent the action event
     */
    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        try {
            logger.info("Refreshing movies...");
            loadMovies();
            showSuccess("Success", "Movies refreshed successfully.");
        } catch (Exception e) {
            logger.error("Error refreshing movies", e);
            showError("Error", "Failed to refresh movies: " + e.getMessage());
        }
    }


    /**
     * Shows advanced search dialog
     * @param event the action event
     */
    @FXML
    private void showAdvancedSearchDialog(ActionEvent event) {
        // Create a custom dialog
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Advanced Search");
        dialog.setHeaderText("Enter your search criteria");
        dialog.setDialogPane(new DialogPane());

        // Set the button types
        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

        // Create the search criteria form
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
        TextField actorsField = new TextField();
        actorsField.setPromptText("Actors");
        TextField genresField = new TextField();
        genresField.setPromptText("Genres");

        // Add sort options
        ComboBox<String> sortByCombo = new ComboBox<>();
        sortByCombo.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High-Low)", "Rating (Low-High)");
        sortByCombo.setPromptText("Sort by...");

        // Add genre checkboxes
        VBox genreBox = new VBox(5);
        genreBox.setPadding(new Insets(5));
        ScrollPane genreScrollPane = new ScrollPane(genreBox);
        genreScrollPane.setFitToWidth(true);
        
        // Create checkboxes for each genre with a select all option
        Map<CheckBox, Genre> genreMap = new HashMap<>();
        
        // Add select all checkbox
        CheckBox selectAll = new CheckBox("Select All Genres");
        selectAll.setSelected(true);
        selectAll.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            genreMap.keySet().forEach(cb -> cb.setSelected(isSelected));
        });
        //selectAll checkbox to the genreBox
        genreBox.getChildren().add(selectAll);
        
        // Add genre checkboxes
        for (Genre genre : Genre.values()) {
            CheckBox checkBox = new CheckBox(genre.name());
            genreBox.getChildren().add(checkBox);
            genreMap.put(checkBox, genre);
        }

        // Add components to grid
        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(new Label("Director:"), 0, row);
        grid.add(directorField, 1, row++);
        grid.add(new Label("Actors:"), 0, row);
        grid.add(actorsField, 1, row++);

        grid.add(new Label("Year Range:"), 0, row);
        grid.add(yearFromField, 1, row++);
        grid.add(yearToField, 2, row++);

        grid.add(new Label("Rating Range:"), 0, row);
        grid.add(ratingFromField, 1, row++);
        grid.add(ratingToField, 2, row++);

        grid.add(new Label("Genres:"), 0, row);
        grid.add(genreScrollPane, 1, row++);


        HBox yearBox = new HBox(5);
        yearBox.getChildren().addAll(yearFromField, new Label("to"), yearToField);
        grid.add(yearBox, 1, row++);
        
        // Add sort option
        grid.add(new Label("Sort By:"), 0, row);
        grid.add(sortByCombo, 1, row++);

        // Add some padding
        grid.setPadding(new Insets(15));

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default
        Platform.runLater(titleField::requestFocus);

        // Convert the result to a map of search criteria when the search button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {


                // Create a map of search criteria
                Map<String, Object> searchCriteria = new HashMap<>();
                if (!titleField.getText().isEmpty()) searchCriteria.put("title", titleField.getText().trim());
                if (!directorField.getText().isEmpty()) searchCriteria.put("director", directorField.getText().trim());
                if (!yearFromField.getText().isEmpty()) searchCriteria.put("yearFrom", Integer.parseInt(yearFromField.getText().trim()));
                if (!yearToField.getText().isEmpty()) searchCriteria.put("yearTo", Integer.parseInt(yearToField.getText().trim()));
                if (!ratingFromField.getText().isEmpty()) searchCriteria.put("ratingFrom", Double.parseDouble(ratingFromField.getText().trim()));
                if (!ratingToField.getText().isEmpty()) searchCriteria.put("ratingTo", Double.parseDouble(ratingToField.getText().trim()));
                if (!actorsField.getText().isEmpty()) searchCriteria.put("actors", actorsField.getText().trim());
                if (!genresField.getText().isEmpty()) searchCriteria.put("genres", genresField.getText().trim());

                
                // Add sort option
                if (sortByCombo.getValue() != null) {
                    searchCriteria.put("sortBy", sortByCombo.getValue());
                }
                
                // Get selected genres (only if not all are selected)
                List<Genre> selectedGenres = genreMap.entrySet().stream()
                    .filter(entry -> entry.getKey().isSelected())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
                
                // Only add genres filter if not all genres are selected
                if (!selectedGenres.isEmpty() && selectedGenres.size() < genreMap.size()) {
                    searchCriteria.put("genres", selectedGenres);
                }
                
                // Add year range if provided
                if (!yearFromField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("yearFrom", Integer.parseInt(yearFromField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid year format
                        showError("Invalid Year", "Please enter a valid year number");
                        return null;
                    }
                }
                if (!yearToField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("yearTo", Integer.parseInt(yearToField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid year format
                        showError("Invalid Year", "Please enter a valid year number");
                        return null;
                    }
                }

                // Add rating range if provided for from-to
                if (!ratingFromField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("ratingFrom", Double.parseDouble(ratingFromField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid rating format
                        showError("Invalid Rating", "Please enter a valid rating number");
                        return null;
                    }
                }

                if (!ratingToField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("ratingTo", Double.parseDouble(ratingToField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid rating format
                        showError("Invalid Rating", "Please enter a valid rating number");
                        return null;
                    }
                }

                return searchCriteria;
            }
            return null;
        });
        
        // Show the dialog and process the results
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(searchCriteria -> {
            try {
                // Apply the search criteria to filter movies
                Set<String> uniqueKeys = new HashSet<>();
                List<Movie> filteredMovies = allMovies.stream()
                        .filter(movie -> {
                            // Create a unique key for each movie (title + year)
                            String uniqueKey = movie.getTitle().toLowerCase() + "_" +
                                    (movie.getReleaseDate() != null ?
                                            new java.text.SimpleDateFormat("yyyy").format(movie.getReleaseDate()) : "");

                            // Skip if we've already seen this movie
                            if (uniqueKeys.contains(uniqueKey)) {
                                return false;
                            }

                            boolean matches = true;

                            // Filter by title (case-insensitive partial match)
                            if (searchCriteria.containsKey("title")) {
                                String searchTitle = ((String) searchCriteria.get("title")).toLowerCase();
                                matches = movie.getTitle().toLowerCase().contains(searchTitle);
                                if (!matches) return false;
                            }

                            // Filter by director (case-insensitive partial match)
                            if (searchCriteria.containsKey("director") && movie.getDirector() != null) {
                                String searchDirector = ((String) searchCriteria.get("director")).toLowerCase();
                                matches = movie.getDirector().toLowerCase().contains(searchDirector);
                                if (!matches) return false;
                            }

                            // Filter by year range
                            if (searchCriteria.containsKey("yearFrom")) {
                                int yearFrom = (int) searchCriteria.get("yearFrom");
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(movie.getReleaseDate());
                                matches = cal.get(Calendar.YEAR) >= yearFrom;
                                if (!matches) return false;
                            }

                            if (searchCriteria.containsKey("yearTo")) {
                                int yearTo = (int) searchCriteria.get("yearTo");
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(movie.getReleaseDate());
                                matches = cal.get(Calendar.YEAR) <= yearTo;
                                if (!matches) return false;
                            }

                            // Filter by genres (must match all selected genres)
                            if (searchCriteria.containsKey("genres")) {
                                @SuppressWarnings("unchecked")
                                List<Genre> selectedGenres = (List<Genre>) searchCriteria.get("genres");
                                matches = new HashSet<>(movie.getGenres()).containsAll(selectedGenres);
                                if (!matches) return false;
                            }

                            // Filter by actors (case-insensitive partial match)
                            if (searchCriteria.containsKey("actors")) {
                                String searchActors = ((String) searchCriteria.get("actors")).toLowerCase();
                                matches = movie.getActors().stream()
                                        .anyMatch(actor -> actor.toLowerCase().contains(searchActors));
                                if (!matches) return false;
                            }

                            // If we get here, the movie matches all criteria
                            uniqueKeys.add(uniqueKey);
                            return true;
                        }).sorted(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());

                // Sort the filtered movies

                // Set the filtered movies to the table
                movieTable.setItems(FXCollections.observableList(filteredMovies));

                
                // Apply sorting if specified
                if (searchCriteria.containsKey("sortBy")) {
                    String sortOption = (String) searchCriteria.get("sortBy");
                    switch (sortOption) {
                        case "Title (A-Z)":
                            filteredMovies.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));
                            break;
                        case "Title (Z-A)":
                            filteredMovies.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                            break;
                        case "Year (Newest)":
                            filteredMovies.sort(Comparator.comparing(
                                Movie::getYear
                            ));
                            break;
                        case "Rating (High-Low)":
                            filteredMovies.sort(Comparator.comparing(
                                Movie::getRating,
                                Comparator.nullsLast(Comparator.<Double>reverseOrder())
                            ));
                            break;
                        case "Rating (Low-High)":
                            filteredMovies.sort(Comparator.comparing(Movie::getRating,
                                Comparator.nullsLast(Comparator.<Double>naturalOrder())));
                            break;
                        default:
                            break;
                    }
                }

                // Update the UI with filtered and sorted results
                Platform.runLater(() -> {
                    allMovies.setAll(filteredMovies);
                    filterMovies(); // Apply any additional filtering from the search bar
                    statusLabel.setText(String.format("Found %d matching movie%s", 
                        filteredMovies.size(),
                        filteredMovies.size() != 1 ? "s" : ""));
                    
                    if (filteredMovies.isEmpty()) {
                        showInformation("No Results", "No movies match your search criteria.");
                        statusLabel.setText("No movies match your search criteria.");
                        movieTable.setItems(FXCollections.observableList(filteredMovies));
                        movieTable.refresh();
                    }
                });
                
            } catch (Exception e) {
                logger.error("Error performing advanced search", e);
                showError("Search Error", "An error occurred while performing the search: " + e.getMessage());
            }
        });

    }

    /**
     * Shows a warning dialog with the specified title and message.
     * This method is thread-safe and can be called from any thread.
     *
     * @param title   The title of the warning dialog
     * @param message The warning message to display
     */
    private void showWarning(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Shows an information dialog with the specified title and content.
     * 
     * @param title   The title of the dialog
     * @param content The content message to display
     */
    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows all movies that have been rated by any user
     */
    @FXML
    private void showMyRatedMovies() {
        try {
            List<Movie> ratedMovies;
            
            try {
                int currentUserId = getCurrentUserId();
                // If user is logged in, show only their rated movies
                final int finalUserId = currentUserId;
                ratedMovies = allMovies.stream()
                    .filter(movie -> !movie.getUserRatings().isEmpty())
                    .filter(movie -> movie.getUserRatings().containsKey(finalUserId))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                // If not logged in, show all rated movies
                ratedMovies = allMovies.stream()
                    .filter(movie -> !movie.getUserRatings().isEmpty())
                    .collect(Collectors.toList());
            }
                
            if (ratedMovies.isEmpty()) {
                showInformation("No Rated Movies", "No movies have been rated yet.");
                return;
            }
            
            // Update the filtered movies list and refresh the table
            filteredMovies.setAll(ratedMovies);
            movieTable.setItems(filteredMovies);
            updateItemCount();
            
        } catch (Exception e) {
            logger.error("Error loading rated movies: {}", e.getMessage(), e);
            showError("Error", "Failed to load rated movies.");
        }
    }
}
