package com.papel.imdb_clone.controllers.coordinator;

import com.papel.imdb_clone.controllers.content.MoviesController;
import com.papel.imdb_clone.controllers.content.SeriesController;
import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Coordinates UI components and manages controller lifecycle.
 * Responsible for loading FXML views and initializing controllers.
 * This class serves as the central coordinator for UI navigation and view management.
 */
public class UICoordinator {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(UICoordinator.class);

    private static UICoordinator instance;
    private final SeriesService seriesService;
    private final MoviesService moviesService;

    private final DataManager dataManager;
    private MoviesService movieService;

    /**
     * Private constructor to prevent instantiation which means it can't be created from outside the class
     */
    private UICoordinator() {
        this.dataManager = DataManager.getInstance();
        this.moviesService = MoviesService.getInstance();
        this.seriesService = SeriesService.getInstance();

        logger.info("UICoordinator initialized with default data manager and content services");
    }

    /**
     * Returns the singleton instance of UICoordinator
     * @return the singleton instance
     */
    public static synchronized UICoordinator getInstance() {
        if (instance == null) {
            instance = new UICoordinator();
        }
        return instance;
    }


    //Primary stage and current user
    private Stage primaryStage;
    private User currentUser;

    // Views
    private Node movieView;
    private Node seriesView;
    private Node searchView;
    private Node homeView;

    private boolean isViewLoading = false;
    private Node directorsAndActorsView;

    /**
     * Constructs a new UICoordinator with the specified data manager.
     *
     * @param dataManager The data manager instance to be used for data operations
     * @throws IllegalArgumentException if dataManager is null
     */
    public UICoordinator(DataManager dataManager) {
        if (dataManager == null) {
            throw new IllegalArgumentException("DataManager cannot be null");
        }
        this.dataManager = dataManager;
        this.moviesService = MoviesService.getInstance();
        this.seriesService = SeriesService.getInstance();
        logger.info("UICoordinator initialized with content services");
    }

    /**
     * Sets the primary stage for the application with proper initialization.
     *
     * @param primaryStage The primary stage instance (must not be null)
     * @throws IllegalArgumentException if primaryStage is null
     */
    public void setPrimaryStage(Stage primaryStage) {
        if (primaryStage == null) {
            logger.error("Attempted to set null primary stage in UICoordinator");
            throw new IllegalArgumentException("Primary stage cannot be null");
        }

        // Only update if this is a new stage
        if (this.primaryStage != primaryStage) {
            logger.info("Setting primary stage in UICoordinator");
            this.primaryStage = primaryStage;

        }
    }


    /**
     * Loads and initializes all views and their controllers.
     *
     * @return true if all views were loaded successfully, false otherwise
     * @throws IllegalArgumentException if primary stage is null
     */
    public boolean loadAndInitializeViews() throws IllegalArgumentException {
        if (primaryStage == null) {
            logger.error("Cannot load views: Primary stage is not set");
            return true;
        }

        logger.info("Loading and initializing all views");
        boolean allViewsLoaded = true;

        // First, ensure the content view is loaded
        if (homeView == null) {
            try {
                loadContentView();
                logger.info("Successfully loaded content view");
            } catch (Exception e) {
                logger.error("Critical: Failed to load content view: {}", e.getMessage(), e);
                return true; // Can't continue without content view-main gui view of the project
            }
        }

        // Define views to load with their names for better error reporting:supplier is a functional interface that returns a value
        Map<String, Supplier<Node>> viewsToLoad = Map.of(
                "movie view", () -> movieView = loadViewSafely("/fxml/content/movie-view.fxml"),
                "series view", () -> seriesView = loadViewSafely("/fxml/content/series-view.fxml"),
                "directors & actors", () -> directorsAndActorsView = loadViewSafely("/fxml/celebrities/celebrities-view.fxml"),
                "search view", () -> searchView = loadViewSafely("/fxml/search/advanced-search-view.fxml")
        );

        // Load each view and track failures
        for (Map.Entry<String, Supplier<Node>> entry : viewsToLoad.entrySet()) {
            String viewName = entry.getKey();
            logger.debug("Loading {}...", viewName);

            try {
                // Load the view using the supplier
                Node view = entry.getValue().get();
                if (view == null) {
                    logger.error("Failed to load {}", viewName);
                    allViewsLoaded = false;
                } else {
                    // Log success of loading the view
                    logger.debug("Successfully loaded {}", viewName);
                }
            } catch (IllegalArgumentException e) {
                logger.error("Error loading {}: {}", viewName, e.getMessage(), e);
                allViewsLoaded = false;
            }
        }

        // Log the result of the view loading process
        if (!allViewsLoaded) {
            logger.warn("Some views failed to load, but continuing with available views");
        } else {
            logger.info("All views loaded successfully");
        }

        return !allViewsLoaded;
    }

    /**
     * Safely loads a view and returns null if loading fails.
     * Errors are logged but not rethrown.
     *
     * @param fxmlPath the path to the FXML file relative to the resources directory
     * @return the loaded JavaFX Node or null if loading fails
     */
    private Node loadViewSafely(String fxmlPath) {
        try {
            // Load the view using the supplier
            return loadView(fxmlPath);
        } catch (Exception e) {
            logger.error("Failed to load view {}: {}", fxmlPath, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads an FXML view from the specified path.
     *
     * @param fxmlPath the path to the FXML file relative to the resources directory
     * @return the loaded JavaFX Node
     * @throws IOException if the FXML file cannot be loaded or parsed
     */
    private Node loadView(String fxmlPath) throws IOException {
        logger.debug("Loading view: {}", fxmlPath);
        java.net.URL resourceUrl = null;
        
        try {
            // Get the resource URL for better error reporting
            resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                // Try with a leading slash if not found
                if (!fxmlPath.startsWith("/")) {
                    resourceUrl = getClass().getResource("/" + fxmlPath);
                }
                
                if (resourceUrl == null) {
                    String errorMsg = String.format("FXML file not found: %s. Searched in classpath: %s",
                            fxmlPath,
                            System.getProperty("java.class.path"));
                    logger.error(errorMsg);
                    throw new IOException(errorMsg);
                }
            }

            logger.debug("Loading FXML from URL: {}", resourceUrl);
            FXMLLoader loader = new FXMLLoader(resourceUrl);

            try {
                //Load the view
                Node view = loader.<Node>load();
                logger.info("Successfully loaded view: {}", fxmlPath);
                return view;
            } catch (Exception e) {
                //Log the error
                String errorMsg = String.format("Failed to load FXML %s: %s", fxmlPath, e.getMessage());
                logger.error(errorMsg, e);
                
                // Check for common FXML loading issues
                if (e.getCause() != null) {
                    logger.error("Root cause: {}", e.getCause().getMessage());
                }
                
                throw new IOException(errorMsg, e);
            }
        } catch (IOException e) {
            logger.error("I/O error loading view {}: {}", fxmlPath, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error loading view {}: {}", fxmlPath, e.getMessage(), e);
            throw new IOException("Failed to load view: " + e.getMessage(), e);
        }
    }


/**
 * Loads the main content view of the application.
 * This is a special case view that needs to be loaded first as it contains the main layout.
 *
 * @throws IOException if the home view FXML file cannot be loaded
 */
private void loadContentView() throws IOException {
    if (homeView != null) {
        return; // Already loaded
    }

    if (isViewLoading) {
        logger.warn("Attempted to load content view while another load is in progress");
        return;
    }

    isViewLoading = true;
    try {
        logger.info("Loading main content view...");
        
        // Load movie view
        try {
            FXMLLoader movieLoader = new FXMLLoader(getClass().getResource("/fxml/content/movie-view.fxml"));
            movieView = movieLoader.load();
            MoviesController moviesController = movieLoader.getController();
            moviesController.setContentService(movieService);
            
            if (currentUser != null) {
                moviesController.initialize(currentUser.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to load movie view: {}", e.getMessage(), e);
            // Continue with other views even if one fails
        }
        
        // Load series view
        try {
            FXMLLoader seriesLoader = new FXMLLoader(getClass().getResource("/fxml/content/series-view.fxml"));
            seriesView = seriesLoader.load();
            SeriesController seriesController = seriesLoader.getController();
            seriesController.setContentService(seriesService);
            
            if (currentUser != null) {
                seriesController.initialize(currentUser.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to load series view: {}", e.getMessage(), e);
            // Continue with other views even if one fails
        }
        
        // Load home view
        try {
            homeView = loadView("/fxml/base/home-view.fxml");
        } catch (Exception e) {
            logger.error("Failed to load home view: {}", e.getMessage(), e);
            // Continue with other views even if one fails
        }
        
        // Load search view
        try {
            searchView = loadView("/fxml/search/advanced-search-view.fxml");
        } catch (Exception e) {
            logger.error("Failed to load search view: {}", e.getMessage(), e);
            // Create a simple error view as fallback
            Label errorLabel = new Label("Failed to load search view. Please check the logs for details.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 20;");
            searchView = errorLabel;
            // Continue with other views even if one fails
        }
        
        logger.info("View loading completed");
    } finally {
        isViewLoading = false;
    }
}

    // Reference to controllers that need auth state updates
    private MoviesController moviesController;
    private SeriesController seriesController;
    
    /**
     * Notifies all relevant controllers about authentication state changes.
     *
     * @param isAuthenticated whether the user is authenticated
     * @param user the current user, or null if logged out
     */
    public void notifyAuthStateChanged(boolean isAuthenticated, User user) {
        logger.info("Authentication state changed. Authenticated: {}", isAuthenticated);
        
        // Update the current user in the data manager's user repository
        if (isAuthenticated && user != null) {
            try {
                // First check if user exists by ID (more reliable than username)
                if (user.getId() > 0) {
                    // First try to find by ID
                    Optional<User> existingUser = dataManager.getUserRepository().findById(user.getId());
                    
                    if (existingUser.isPresent()) {
                        // User exists in repository, update local user reference to the one from repository
                        logger.debug("User found in repository (ID: {}): {}", user.getId(), user.getUsername());
                        this.currentUser = existingUser.get();
                    } else {
                        // If not found by ID, try to find by username
                        Optional<User> userByUsername = dataManager.getUserRepository().findByUsername(user.getUsername());
                        
                        if (userByUsername.isPresent()) {
                            // User exists but with different ID - use the repository's version
                            logger.debug("User found by username but with different ID. Using repository ID: {} instead of {}", 
                                    userByUsername.get().getId(), user.getId());
                            this.currentUser = userByUsername.get();
                        } else {
                            // User doesn't exist in repository, try to add
                            logger.debug("Adding new user to repository (ID: {}): {}", user.getId(), user.getUsername());
                            try {
                                // Try to save the user
                                User savedUser = dataManager.getUserRepository().save(user);
                                this.currentUser = savedUser;
                            } catch (IllegalArgumentException e) {
                                // If save fails, log the error but continue with the provided user
                                logger.error("Failed to save user to repository: {}", e.getMessage());
                                this.currentUser = user;
                            }
                        }
                    }
                } else {
                    logger.warn("Cannot update repository: User has invalid ID: {}", user);
                    this.currentUser = user; // Still set the current user even if ID is invalid
                }
            } catch (Exception e) {
                logger.error("Error updating user in repository: {}", e.getMessage(), e);
                // Continue with the provided user even if repository update fails
                this.currentUser = user;
            }
        } else {
            // Not authenticated, clear current user
            this.currentUser = null;
        }
        
        // Notify all controllers that need to know about auth state changes
        if (moviesController != null) {
            try {
                if (isAuthenticated && user != null) {
                    // Use -1 or another invalid ID for unauthenticated users
                    moviesController.initializeController(user.getId());
                } else {
                    // Pass a default invalid user ID for unauthenticated users
                    moviesController.initializeController(-1);
                }
                logger.debug("Notified MoviesController about auth state change");
            } catch (Exception e) {
                logger.error("Error notifying MoviesController about auth state change: {}", e.getMessage(), e);
            }
        }
        //Notify SeriesController about auth state change
        if (seriesController != null) {
            try {
                if (isAuthenticated && user != null) {
                    seriesController.initializeController(user.getId());
                } else {
                    seriesController.initializeController(-1);
                }
                logger.debug("Notified SeriesController about auth state change");
            } catch (Exception e) {
                logger.error("Error notifying SeriesController about auth state change: {}", e.getMessage(), e);
            }
        }

    }

    /**
     * Gets the home view component.
     *
     * @return the Node containing the home view, or null if not loaded
     */
    public Node getHomeView() {
        if (homeView == null) {
            logger.warn("Home view was not preloaded, attempting to load on demand");
            try {
                // Try to load just the home view
                homeView = loadView("/fxml/base/home-view.fxml");
                logger.info("Successfully loaded home view on demand");
            } catch (Exception e) {
                logger.error("Critical: Failed to load home view on demand: {}", e.getMessage(), e);
                // Create a simple error view as fallback
                Label errorLabel = new Label("Failed to load home view. Please check the logs for details.");
                errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 20;");
                homeView = errorLabel;
            }
        }
        // Return the loaded home view
        logger.info("Home view loaded successfully");
        return homeView;
    }

}