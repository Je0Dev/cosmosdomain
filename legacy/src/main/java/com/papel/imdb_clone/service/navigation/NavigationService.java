package com.papel.imdb_clone.service.navigation;

import com.papel.imdb_clone.controllers.content.EditContentController;
import com.papel.imdb_clone.controllers.content.MoviesController;
import com.papel.imdb_clone.controllers.people.CelebritiesController;
import com.papel.imdb_clone.controllers.MainController;
import com.papel.imdb_clone.controllers.content.SeriesController;
import com.papel.imdb_clone.controllers.search.AdvancedSearchController;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.service.validation.AuthService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Service for handling navigation between views.
 */
public class NavigationService {

    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);//loader
    private static NavigationService instance;

    private final Stack<Scene> sceneStack = new Stack<>();//scene stack for navigation
    private final Map<String, Object> userData = new HashMap<>(); //user data for navigation

    /**
     * Private constructor to enforce singleton pattern and module encapsulation.
     */
    private NavigationService() {
        // Private constructor to prevent direct instantiation
    }

    //current controller
    private Object currentController;


    //return singleton instance of NavigationService which means that only one instance of NavigationService can exist at a time
    public static synchronized NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    /**
     * Navigate to a new view.
     *
     * @param fxmlPath     Path to the FXML file relative to resources
     * @param data         Data to pass to the controller
     * @param currentStage The current stage to update
     * @param title        Title for the new window
     */
    public void navigateTo(String fxmlPath, Map<String, Object> data, Stage currentStage, String title) {
        try {
            if (fxmlPath == null || fxmlPath.trim().isEmpty()) {
                throw new IllegalArgumentException("FXML path cannot be null or empty");
            }

            // Get the current stage if not provided
            if (currentStage == null) {
                currentStage = ServiceLocator.getPrimaryStage();
                if (currentStage == null) {
                    throw new IllegalStateException("No stage provided and primary stage is not set");
                }
            }

            // Get the current scene and root
            Scene currentScene = currentStage.getScene();
            if (currentScene == null) {
                // First time loading, just load the full scene
                loadFullScene(fxmlPath, currentStage, title);
                return;
            }

            // Check if we have a BorderPane as root
            if (currentScene.getRoot() instanceof BorderPane) {
                BorderPane rootPane = (BorderPane) currentScene.getRoot();
                Node sidebar = rootPane.getLeft();
                
                // Load the new content
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                if (loader.getLocation() == null) {
                    throw new IOException("FXML file not found: " + fxmlPath);
                }
                
                Node newContent = loader.load();
                
                // Initialize the controller
                Object controller = loader.getController();
                if (controller != null) {
                    initializeController(controller);
                }
                
                // Update only the center content
                rootPane.setCenter(newContent);
                
                // Ensure sidebar is preserved
                if (rootPane.getLeft() == null && sidebar != null) {
                    rootPane.setLeft(sidebar);
                }
                
                // Update title and store controller
                currentStage.setTitle(title);
                this.currentController = controller;
                
                logger.debug("Updated center content for: " + title);
            } else {
                // Not a BorderPane, load full scene
                loadFullScene(fxmlPath, currentStage, title);
            }
            
        } catch (Exception e) {
            String errorMsg = String.format("Failed to navigate to %s: %s", title, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    private void loadFullScene(String fxmlPath, Stage stage, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        // Initialize controller if it exists
        Object controller = loader.getController();
        if (controller != null) {
            initializeController(controller);
        }

        //create new scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();

        //set current controller
        this.currentController = controller;
    }

    /**
     * Initialize the controller.
     * @param controller The controller to initialize
     * @throws Exception if initialization fails
     */
    private void initializeController(Object controller) throws Exception {
        // Get the current session token from user data
        String sessionToken = (String) getUserData("sessionToken");
        int currentUserId = -1;
        
        // Initialize AuthService and get current user ID if session exists
        AuthService authService = AuthService.getInstance();
        if (sessionToken != null && !sessionToken.isEmpty()) {
            currentUserId = authService.getCurrentUserId(sessionToken);
            logger.debug("Current user ID: {}", currentUserId);
        } else {
            logger.debug("No session token found, using default user ID (-1)");
        }

        // Handle other controller initializations if needed
        if (controller instanceof com.papel.imdb_clone.controllers.BaseController) {
            ((com.papel.imdb_clone.controllers.BaseController) controller).initialize(currentUserId);
        }

        //initialize main controller
        if (controller instanceof MainController) {

            ((MainController) controller).setPrimaryStage(ServiceLocator.getPrimaryStage());

            //initialize movies controller
        } else if (controller instanceof MoviesController) {

            MoviesService moviesService = MoviesService.getInstance();
            ((MoviesController) controller).setContentService(moviesService);
            ((MoviesController) controller).initializeController(currentUserId);

            //initialize series controller
        } else if (controller instanceof SeriesController) {

            SeriesService seriesService = SeriesService.getInstance();
            ((SeriesController) controller).setContentService(seriesService);
            ((SeriesController) controller).initializeController(currentUserId);

            //initialize advanced search controller
        } else if (controller instanceof AdvancedSearchController) {

            logger.debug("AdvancedSearchController initialized");
            //initialize edit content controller
        } else if (controller instanceof EditContentController) {

            logger.debug("EditContentController initialized");

            // Get content type from user data and ensure it's set

            //get content type from user data
            String contentType = (String) getUserData("contentType");

            if (contentType == null || contentType.trim().isEmpty()) {
                logger.warn("No content type specified for EditContentController. Defaulting to 'movie'.");
                contentType = "movie";
                setUserData("contentType", contentType);
            }
            logger.info("Initializing EditContentController with content type: {}", contentType);
        } else if (controller instanceof CelebritiesController) {
            logger.debug("CelebritiesController initialized");
        } else {
            logger.warn("Unknown controller type: {}", controller.getClass().getName());
        }
    }


    /**
     * Navigate to the celebrities view.
     * @param currentStage The current stage to update
     */
    public void showCelebrities(Stage currentStage) {
        try {
            if (currentStage == null) {
                currentStage = ServiceLocator.getPrimaryStage();
                if (currentStage == null) {
                    throw new IllegalStateException("Primary stage is not set. Cannot navigate to Celebrities view.");
                }
            }
            navigateTo("/fxml/celebrities/celebrities-view.fxml", null, currentStage, "Celebrities");
        } catch (Exception e) {
            logger.error("Failed to navigate to celebrities view: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to navigate to celebrities view: " + e.getMessage(), e);
        }
    }

    /**
     * Navigates back to the previous view.
     */
    public void goBack() {
        if (!sceneStack.isEmpty()) {
            Scene previousScene = sceneStack.pop();
            if (previousScene != null) {
                ServiceLocator.getPrimaryStage().setScene(previousScene);
            }
        }
    }

    
    /**
     * Gets the current controller instance.
     * @return The current controller instance, or null if not available
     */
    public Object getCurrentController() {
        return currentController;
    }

    /**
     * Gets user data for the specified content type.
     * @param contentType The type of content to retrieve data for
     * @return The user data object, or null if not found
     */
    public Object getUserData(String contentType) {
        return userData.get(contentType);
    }
    
    /**
     * Sets user data for the specified content type.
     * @param contentType The type of content to store data for
     * @param data The data to store
     */
    public void setUserData(String contentType, Object data) {
        userData.put(contentType, data);
    }

}
