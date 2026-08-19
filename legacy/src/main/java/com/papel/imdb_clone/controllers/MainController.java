package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.validation.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.papel.imdb_clone.util.UIUtils.showError;

/**
 * MainController serves as the primary controller for the application's main window.
 * It manages the overall application state, handles navigation between views, and coordinates
 * between different UI components and services.
 */

public class MainController extends BorderPane {


    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private static final String MOVIES_VIEW = "/fxml/content/movie-view.fxml";
    private static final String SERIES_VIEW = "/fxml/content/series-view.fxml";
    private static final String CELEBRITIES_VIEW = "/fxml/content/celebrities-view.fxml";
    private static final String ADVANCED_SEARCH_VIEW = "/fxml/search/advanced-search-view.fxml";


    // Service
    private final AuthService authService = AuthService.getInstance();

    private UICoordinator uiCoordinator;

    // UI Components
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Button signInButton;
    @FXML private Button registerButton;
    @FXML private Button moviesButton;
    @FXML private Button tvShowsButton;
    @FXML private Button celebritiesButton;
    @FXML private Button ratedButton;
    @FXML private Button advancedSearchButton;
    @FXML private Label guestMessage;
    
    // UI Components
    private VBox homeContent;
    
    // Application state
    private Map<String, Object> data;
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;
    private boolean isInitialized = false;
    private boolean isGuest = true; // Default to guest mode
    
    /**
     * Sets the session token for the current user session.
     * @param sessionToken The session token to set
     */
    public void setSessionToken(String sessionToken) {
        logger.info("Setting session token: {}", sessionToken != null ? 
            sessionToken.substring(0, Math.min(8, sessionToken.length())) + "..." : "null");
        this.sessionToken = sessionToken;
        
        // Update authentication state when session token is set
        if (sessionToken != null) {
            try {
                this.currentUser = authService.getUserFromSession(sessionToken);
                this.isGuest = (this.currentUser == null);
                logger.info("Session token set for user: {}", 
                    this.currentUser != null ? this.currentUser.getUsername() : "null");
                
                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    updateUIForAuthState(!isGuest);
                    updateUserInterface();
                    if (currentUser != null) {
                        updateUIForLoggedInUser(currentUser);
                    }
                });
            } catch (Exception e) {
                logger.error("Error setting session token: {}", e.getMessage(), e);
                this.isGuest = true;
                this.currentUser = null;
                Platform.runLater(() -> updateUIForAuthState(false));
            }
        } else {
            this.isGuest = true;
            this.currentUser = null;
            logger.info("Session token cleared, switching to guest mode");
            Platform.runLater(() -> updateUIForAuthState(false));
        }
    }


    /**
     * Handles navigation to the home view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    public void showHome(ActionEvent event) {
        try {
            logger.info("Navigating to home view");
            
            // Get the current user from the session token first
            User currentUserFromSession = null;
            if (sessionToken != null) {
                currentUserFromSession = authService.getUserFromSession(sessionToken);
                if (currentUserFromSession == null) {
                    // If we have a session token but no user, clear the invalid session
                    logger.warn("Invalid session token found, clearing session");
                    sessionToken = null;
                } else {
                    logger.debug("Found user in session: {}", currentUserFromSession.getUsername());
                }
            }
            
            // Update the current user and guest status
            this.currentUser = currentUserFromSession;
            this.isGuest = (this.currentUser == null);
            
            logger.debug("Current user in showHome: {}", this.currentUser != null ? this.currentUser.getUsername() : "null");
            logger.debug("Session token in showHome: {}", sessionToken != null ? "[exists]" : "null");

            // Clear any existing center content
            Node source = (Node) event.getSource();
            Scene scene = source.getScene();
            if (scene != null && scene.getRoot() instanceof BorderPane rootPane) {

                // Create a new home content panel
                homeContent = new VBox(20);
                homeContent.setPadding(new Insets(40));
                homeContent.setAlignment(Pos.CENTER);
                homeContent.setStyle("-fx-background-color: #0f0f0f;");

                // Add welcome message with user's name if logged in
                String welcomeMessage = !isGuest
                    ? String.format("Welcome back, %s!", this.currentUser.getUsername())
                    : "Welcome to IMDb Clone";

                Label welcomeLabel = new Label(welcomeMessage);
                welcomeLabel.setStyle("-fx-text-fill: #f5c518; -fx-font-size: 36px; -fx-font-weight: bold;");

                String subMessage = !isGuest
                    ? "Continue exploring your favorite movies and TV shows."
                    : "Discover and explore your favorite movies and TV shows.";

                Label subLabel = new Label(subMessage);
                subLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 18px;");
                subLabel.setWrapText(true);

                // Add buttons
                HBox buttonRow = new HBox(20);
                buttonRow.setAlignment(Pos.CENTER);

                Button moviesButton = new Button("Movies");
                moviesButton.setStyle("-fx-background-color: #f5c518; -fx-text-fill: #000000; -fx-font-size: 16px; -fx-font-weight: bold; -fx-pref-width: 150; -fx-pref-height: 60; -fx-cursor: hand; -fx-background-radius: 5;");
                moviesButton.setOnAction(this::showMovies);

                Button tvShowsButton = new Button("TV Shows");
                tvShowsButton.setStyle("-fx-background-color: #f5c518; -fx-text-fill: #000000; -fx-font-size: 16px; -fx-font-weight: bold; -fx-pref-width: 150; -fx-pref-height: 60; -fx-cursor: hand; -fx-background-radius: 5;");
                tvShowsButton.setOnAction(this::showTVShows);

                buttonRow.getChildren().addAll(moviesButton, tvShowsButton);

                // Add content to the home panel
                homeContent.getChildren().addAll(welcomeLabel, subLabel, buttonRow);

                // Clear and set the new content
                rootPane.setCenter(homeContent);
                
                // Update the UI based on authentication state
                updateUIForAuthState(!isGuest);
                
                // Update the user label and other UI elements
                updateUserLabel();
                
                if (currentUser != null) {
                    updateUIForLoggedInUser(currentUser);
                }
                
                // Update the UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    updateUIForAuthState(!isGuest);
                    updateUserLabel();
                    if (currentUser != null) {
                        updateUIForLoggedInUser(currentUser);
                    }
                });
            }

            // Update window title
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setTitle("IMDb Clone - Home");

            // Update the UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    // Get the current user from the service to ensure we have the latest data
                    User user = sessionToken != null ? authService.getCurrentUser(sessionToken) : null;
                    // Get the current scene and root
                    Scene currentScene = this.getScene();
                    BorderPane rootPane = null;

                    // If we have a scene and the root is a BorderPane, use it
                    if (currentScene != null && currentScene.getRoot() instanceof BorderPane) {
                        rootPane = (BorderPane) currentScene.getRoot();
                    }

                    // If no valid root pane, create a new one
                    if (rootPane == null) {
                        rootPane = new BorderPane();
                        if (currentScene == null) {
                            Stage stage = primaryStage != null ? primaryStage : new Stage();
                            currentScene = new Scene(rootPane);
                            stage.setScene(currentScene);
                            stage.show();
                        } else {
                            currentScene.setRoot(rootPane);
                        }
                    }

                    // Set the home content
                    rootPane.setCenter(homeContent);

                    // Ensure the window is visible
                    if (currentScene.getWindow() != null) {
                        currentScene.getWindow().sizeToScene();
                    }

                    // Request focus on the content
                    homeContent.requestFocus();

                    logger.info("Home view loaded successfully");

                } catch (Exception e) {
                    logger.error("Error updating home view: ", e);
                    showError("Navigation Error", "Failed to update home view: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Failed to load home view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load home view: " + e.getMessage());
        }
    }


    /**
     * Handles navigation to the movies view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showMovies(ActionEvent event) {
        if (!checkAuthentication("movies view")) {
            return;
        }
        navigateToView(event, MOVIES_VIEW, "Movies");
    }

    /**
     * Handles navigation to the TV shows view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showTVShows(ActionEvent event) {
        navigateToView(event, SERIES_VIEW, "TV Shows");
    }
    

    /**
     * Checks if the user is authenticated before allowing access to a feature.
     * @param featureName The name of the feature being accessed
     * @return true if authenticated, false otherwise
     */
    private boolean checkAuthentication(String featureName) {
        // Allow all users including guests to access all features
        logger.debug("Access granted to {} view for {}", 
            featureName, sessionToken != null ? "user" : "guest");
        return true;
    }
    
    /**
     * Handles navigation to the advanced search view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showAdvancedSearch(ActionEvent event) {
        if (!checkAuthentication("advanced search")) {
            return;
        }
        navigateToView(event, ADVANCED_SEARCH_VIEW, "Advanced Search");
    }

    /**
     * Updates the UI based on the current authentication state
     */
    private void updateAuthUI() {
        boolean isLoggedIn = currentUser != null;

        // Hide sign in and register buttons for all users
        if (signInButton != null) {
            signInButton.setVisible(false);
            signInButton.setManaged(false);
        }

        if (registerButton != null) {
            registerButton.setVisible(false);
            registerButton.setManaged(false);
        }

        // Update user label - only show for logged in users
        if (userLabel != null) {
            if (isLoggedIn) {
                userLabel.setText("Welcome, " + currentUser.getUsername());
                userLabel.setVisible(true);
                userLabel.setManaged(true);
            } else {
                userLabel.setVisible(false);
                userLabel.setManaged(false);
            }
        }
    }


    /**
     * Common method to handle view navigation.
     *
     * @param event The action event that triggered the navigation
     * @param viewPath The FXML path of the view to navigate to
     * @param title The title to display in the window
     * @throws IllegalStateException if navigation fails
     */
    private void navigateToView(ActionEvent event, String viewPath, String title) {

        // Check for null parameters
        Objects.requireNonNull(event, "ActionEvent cannot be null");
        Objects.requireNonNull(viewPath, "View path cannot be null");

        try {
            // Special handling for home view to prevent sidebar recreation
            if (viewPath.endsWith("home-view.fxml")) {
                showHome(event);
                return;
            }

            // Ensure we have the latest user from the session
            User currentUserFromSession = sessionToken != null ? authService.getUserFromSession(sessionToken) : null;
            this.currentUser = currentUserFromSession;
            this.isGuest = (this.currentUser == null);
            
            logger.debug("Navigating to {} view with user: {}", 
                title, currentUserFromSession != null ? currentUserFromSession.getUsername() : "null");

            Node source = (Node) event.getSource();
            Window window = source.getScene().getWindow();
            if (!(window instanceof Stage)) {
                throw new IllegalStateException("Could not determine the current stage");
            }

            // Create data map with session token and user info
            Map<String, Object> navigationData = new HashMap<>();
            if (data != null) {
                navigationData.putAll(data); // Copy existing data
            }

            // Ensure we have a valid session token
            if (sessionToken == null && currentUser != null) {
                try {
                    sessionToken = authService.getCurrentSessionToken();
                } catch (Exception e) {
                    logger.error("Error getting session token: {}", e.getMessage(), e);
                }
            }

            // Add session token and user info if available
            if (sessionToken != null) {
                navigationData.put("sessionToken", sessionToken);
                if (currentUser != null) {
                    navigationData.put("currentUser", currentUser);
                }
            }

            // Update UI state before navigation
            Platform.runLater(() -> {
                updateUIForAuthState(!isGuest);
                updateUserLabel();
                if (currentUser != null) {
                    updateUIForLoggedInUser(currentUser);
                }
            });

            // Navigate to the new view
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo(viewPath, navigationData, (Stage) window, title);

            logger.debug("Navigated to {} view with session token: {}",
                title, sessionToken != null ? "present" : "not present");

        } catch (Exception e) {
            String errorMsg = String.format("Failed to navigate to %s view: %s", title, e.getMessage());
            logger.error(errorMsg, e);
            showError("Navigation Error", errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }


    /**
     * Default constructor for FXML loader.
     */
    public MainController() {
        // Services will be initialized in the initialize() method
        logger.info("MainController constructor called");
    }


    /**
     * Sets the current user and updates the UI accordingly
     * @param user The user to set as current, or null to log out
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.isGuest = (user == null);

        if (user != null) {
            // If we have a user, ensure we have a valid session
            if (sessionToken == null || authService.getUserFromSession(sessionToken) == null) {
                // Try to get a valid session token
                try {
                    sessionToken = authService.getCurrentSessionToken();
                } catch (Exception e) {
                    logger.error("Error getting session token: {}", e.getMessage(), e);
                    sessionToken = null;
                }
            }
            // Update the UI
            updateUIForAuthState(true);
        } else {

            // Logging out
            sessionToken = null;
            updateUIForAuthState(false);
        }

        updateUserInterface();
    }

    /**
     * Shows the login screen
     * @param event The action event that triggered this method
     */
    @FXML
    private void showLogin(ActionEvent event) {
        try {
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login-view.fxml"));
            Parent root = loader.load();

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Sign In");
            stage.show();

            logger.info("Navigated to login screen");
        } catch (IOException e) {
            logger.error("Failed to load login view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load login screen: " + e.getMessage());
        }
    }

    /**
     * Shows the registration screen
     * @param event The action event that triggered this method
     */
    @FXML
    private void showRegister(ActionEvent event) {
        try {

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Load the register view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/register-view.fxml"));
            Parent root = loader.load();

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.show();

            logger.info("Navigated to register screen");
        } catch (IOException e) {
            logger.error("Failed to load register view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load registration screen: " + e.getMessage());
        }
    }



    /**
     * Checks the current authentication state and updates the UI accordingly.
     * This method verifies if the current session token is still valid.
     */
    private void checkAuthentication() {
        try {
            if (sessionToken != null) {
                // Try to get the current user with the session token
                User user = authService.getCurrentUser(sessionToken);
                if (user != null) {
                    // User is authenticated
                    setCurrentUser(user);
                    updateUIForAuthState(true);
                    return;
                }
            }
            // If we get here, user is not authenticated
            setCurrentUser(null);
            updateUIForAuthState(false);
        } catch (Exception e) {
            logger.error("Error checking authentication: {}", e.getMessage(), e);
            // In case of error, assume not authenticated
            setCurrentUser(null);
            updateUIForAuthState(false);
        }
    }


    /**
     * Initializes the controller and sets up the UI.
     * This method is called by the FXMLLoader when the FXML file is loaded.
     * It sets up the UI coordinator, checks authentication, and sets up button actions.
     * It also updates the UI for the current authentication state.
     */
    @FXML
    public void initialize() {
        try {
            logger.info("Initializing MainController");

            // Initialize UI coordinator
            this.uiCoordinator = UICoordinator.getInstance();

            // Initialize UI state based on authentication
            updateUIForAuthState(authService.isAuthenticated());

            // Set up button actions
            setupButtonActions();

            // Check if we have a logged-in user
            checkAuthentication();

            logger.info("MainController initialized successfully");
            isInitialized = true;
        } catch (Exception e) {
            logger.error("Error initializing MainController: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize the application.");
        }
    }

    /**
     * Sets up button actions for navigation
     */
    private void setupButtonActions() {
        if (moviesButton != null) {
            moviesButton.setOnAction(this::showMovies);
        }
        if (tvShowsButton != null) {
            tvShowsButton.setOnAction(this::showTVShows);
        }
        if (celebritiesButton != null) {
            celebritiesButton.setOnAction(this::showCelebrities);
        }
        if (advancedSearchButton != null) {
            advancedSearchButton.setOnAction(this::showAdvancedSearch);
        }
        if (signInButton != null) {
            signInButton.setOnAction(this::showLogin);
        }
        if (registerButton != null) {
            registerButton.setOnAction(this::showRegister);
        }
    }

    /**
     * Updates the UI based on authentication state
     * @param isAuthenticated whether the user is authenticated
     */
    public void updateUIForAuthState(boolean isAuthenticated) {
        Platform.runLater(() -> {
            logger.debug("Updating UI for authentication state: {}", isAuthenticated ? "AUTHENTICATED" : "GUEST");
            
            // Update authentication state
            this.isGuest = !isAuthenticated;
            
            // Update UI elements based on authentication state
            if (signInButton != null) {
                signInButton.setVisible(!isAuthenticated);
                signInButton.setManaged(!isAuthenticated);
            }
            if (registerButton != null) {
                registerButton.setVisible(!isAuthenticated);
                registerButton.setManaged(!isAuthenticated);
            }
            if (guestMessage != null) {
                guestMessage.setVisible(!isAuthenticated);
                guestMessage.setManaged(!isAuthenticated);
            }
            if (userLabel != null) {
                userLabel.setVisible(isAuthenticated);
                userLabel.setManaged(isAuthenticated);
                if (!isAuthenticated) {
                    userLabel.setText("");
                }
            }
            
            // Update user label and welcome message
            updateUserLabel();
            
            // Enable/disable feature buttons based on authentication
            setFeatureButtonsEnabled(isAuthenticated);
            
            // Log the current state for debugging
            logger.debug("UI Update - , Sign In Button: {}, Register Button: {}, Guest Message: {}",
                (signInButton != null && signInButton.isVisible()),
                (registerButton != null && registerButton.isVisible()),
                (guestMessage != null && guestMessage.isVisible()));
        });
    }
    
    /**
     * Enables or disables feature buttons based on authentication
     * @param enabled whether to enable the buttons
     */
    private void setFeatureButtonsEnabled(boolean enabled) {
        if (moviesButton != null) {
            moviesButton.setDisable(!enabled);
            moviesButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (tvShowsButton != null) {
            tvShowsButton.setDisable(!enabled);
            tvShowsButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (celebritiesButton != null) {
            celebritiesButton.setDisable(!enabled);
            celebritiesButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (ratedButton != null) {
            ratedButton.setDisable(!enabled);
            ratedButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (advancedSearchButton != null) {
            advancedSearchButton.setDisable(!enabled);
            advancedSearchButton.setOpacity(enabled ? 1.0 : 0.5);
        }
    }


    public void updateUserInterface() {
        updateUserLabel();
        updateStatusBar();
        updateUIForAuthState(currentUser != null);
    }

    /**
     * Updates the status bar with the current application state.
     */
    private void updateStatusBar() {
        if (statusLabel == null) {
            return;
        }
        
        String status = currentUser != null 
            ? String.format("Logged in as %s", currentUser.getUsername())
            : "Not logged in";
            
        statusLabel.setText(status);
    }

    /**
     * Updates the user label in the UI to reflect the current user's information.
     * If no user is logged in, it displays "Guest" as the username.
     */
    public void updateUserLabel() {
        if (userLabel == null) {
            logger.warn("userLabel is not initialized in FXML. Cannot update user display.");
            return;
        }
        
        if (currentUser != null) {
            String username = currentUser.getUsername();
            // Log only first few and last few characters of session token for security
            String tokenPreview = sessionToken != null 
                ? String.format("%s...%s", 
                    sessionToken.substring(0, Math.min(4, sessionToken.length())),
                    sessionToken.substring(sessionToken.length() - 4))
                : "<no-token>";
                
            logger.debug("Updating user label for user: {}, session: {}", username, tokenPreview);
            userLabel.setText(username);
            userLabel.getStyleClass().remove("guest-label");
            userLabel.getStyleClass().add("user-label");
        } else {
            logger.debug("No user logged in, showing guest mode");
            userLabel.setText("Guest");
            userLabel.getStyleClass().remove("user-label");
            userLabel.getStyleClass().add("guest-label");
        }
    }

    /**
     * Sets the primary stage for the application.
     * This method must be called exactly once during application startup,
     * before any UI operations are performed.
     *
     * @param primaryStage The primary stage of the JavaFX application (must not be null)
     * @throws IllegalStateException if the primary stage is already set
     * @throws IllegalArgumentException if primaryStage is null
     */
    public void setPrimaryStage(Stage primaryStage) {
        Objects.requireNonNull(primaryStage, "Primary stage cannot be null");

        synchronized (this) {
            if (this.primaryStage != null) {
                String errorMsg = "Primary stage is already set";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            
            this.primaryStage = primaryStage;
            logger.info("Primary stage set successfully");
            
            // Configure primary stage properties
            configurePrimaryStage(primaryStage);
        }
    }
    
    /**
     * Configures the primary stage with default settings.
     * 
     * @param stage The primary stage to configure
     */
    private void configurePrimaryStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            logger.info("Application shutdown requested");
        });
        
        // Set minimum size constraints for the main stage-home view
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        
        // Set window title
        stage.setTitle("IMDb Clone");
        
        // Set up window state change listeners
        stage.iconifiedProperty().addListener((obs, wasIconified, isNowIconified) -> {
            logger.debug("Window {}minimized", isNowIconified ? "" : "not ");
        });
    }

    
    // Navigate to celebrities view
    @FXML
    public void showCelebrities(ActionEvent actionEvent) {
        try {
            if (primaryStage == null) {
                primaryStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            }
            //Navigate to celebrities view
            NavigationService.getInstance().showCelebrities(primaryStage);
        } catch (Exception e) {
            logger.error("Failed to navigate to celebrities view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to open celebrities view: " + e.getMessage());
        }
    }

    public void setGuest(boolean b) {
        this.isGuest = b;
        updateAuthUI();
    }

    public void updateUIForLoggedInUser(User user) {
        Platform.runLater(() -> {
            logger.info("Updating UI for logged in user: {}", user != null ? user.getUsername() : "null");
            this.currentUser = user;
            this.isGuest = (user == null);
            
            // Update UI components
            updateUIForAuthState(!this.isGuest);
            updateUserLabel();
            updateStatusBar();
            
            // Notify all controllers about the authentication state change
            if (uiCoordinator != null) {
                uiCoordinator.notifyAuthStateChanged(!this.isGuest, user);
            }
            
            // Force a UI refresh
            updateUserInterface();
            
            logger.debug("UI update completed for user: {}", user != null ? user.getUsername() : "null");
        });
    }

    //set user
    public void setUser(User user) {
        this.currentUser = user;
        this.isGuest = false;
        updateUIForLoggedInUser(user);
        updateUserInterface();
    }

    //set stage
    public void setStage(Stage stage) {
        this.primaryStage = stage;
        updateUserInterface();
    }
}
