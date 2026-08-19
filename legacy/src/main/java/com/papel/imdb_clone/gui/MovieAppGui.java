package com.papel.imdb_clone.gui;

import com.papel.imdb_clone.config.ApplicationConfig;
import com.papel.imdb_clone.controllers.authentication.AuthController;
import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Improved IMDB Clone Application using FXML and modern JavaFX practices with authentication
 */
public class MovieAppGui extends Application {


    private static final Logger logger = LoggerFactory.getLogger(MovieAppGui.class);

    private static final String LOGIN_VIEW = "/fxml/auth/login-view.fxml";
    private static final String REGISTER_VIEW = "/fxml/auth/register-view.fxml";
    private Stage authStage; // Reference to the authentication stage


    /**
     * Constructs a new MovieAppGui instance.
     */
    public MovieAppGui() {
        // Initialize the application configuration
        ApplicationConfig.getInstance();
        // Initialize the service locator
        ServiceLocator.getInstance();
        // Initialize the data manager
        DataManager.getInstance();
    }

    private ApplicationConfig config; // Application configuration
    private ServiceLocator serviceLocator; // Service locator

    /**
     * Initializes the application
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception if the application fails to start
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // Set the primary stage in ServiceLocator as early as possible
        ServiceLocator.setPrimaryStage(primaryStage);

        try {
            System.out.println("[Startup] Entering start()...");

            initializeApplication();

            System.out.println("[Startup] initializeApplication() completed");

            // Set window properties
            primaryStage.setTitle(config.getAppTitle());
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.setWidth(1400);
            primaryStage.setHeight(900);
            primaryStage.setResizable(true);

            logger.info("Application started successfully");

            // Show login screen first
            showAuthScreen(LOGIN_VIEW, "Login");
            System.out.println("[Startup] Login screen shown");

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            Platform.exit();
        }
    }

    /**
     * Initializes the application
     * @throws IOException if the application fails to initialize
     */
    private void initializeApplication() throws IOException {
        logger.info("Initializing IMDB Clone application...");

        // Load configuration
        config = ApplicationConfig.getInstance();

        // Initialize service locator and get data manager
        serviceLocator = ServiceLocator.getInstance();

        // Create and register AuthController
        AuthController authController = new AuthController();
        serviceLocator.registerService(AuthController.class, authController);
        logger.info("Registered AuthController with ServiceLocator");

        // Get data manager using the new getDataManager() method
        DataManager dataManager = serviceLocator.getDataManager();
        dataManager.loadAllData();

        logger.info("Application initialization complete");
    }

    /**
     * Shows the authentication screen (login or register)
     * @param fxmlPath Path to the FXML file to load
     * @param title Title for the authentication window
     */
    private void showAuthScreen(String fxmlPath, String title) {
        try {

            // Log the creation of the authentication screen
            System.out.println("[" + title + "] Creating " + title.toLowerCase() + " screen...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (authStage == null) {
                authStage = new Stage();
                authStage.setTitle(title + " - " + config.getAppTitle());
                authStage.setMinWidth(1000);
                authStage.setMinHeight(700);
                authStage.setResizable(true);
                authStage.centerOnScreen();
                authStage.show();
            } else {
                authStage.getScene().setRoot(root);
            }
            
            // Set the stage in the controller if it's an AuthController
            Object controller = loader.getController();
            if (controller instanceof AuthController) {
                ((AuthController) controller).setStage(authStage);
            }
            
            // Set up the scene if it's the first time
            if (authStage.getScene() == null) {
                Scene scene = new Scene(root, 1200, 800);
                authStage.setScene(scene);
            }

            // Show the authentication screen
            System.out.println("[" + title + "] Showing " + title.toLowerCase() + " screen...");
            if (!authStage.isShowing()) {
                authStage.show();
            }
            
        } catch (Exception e) {
            // Log the error
            System.err.println("[" + title + "][ERROR] Failed to show " + title.toLowerCase() + " screen: " + e);
            showErrorAndExit(e);
            Platform.exit();
        }
    }


    /**
     * Shows an error message and exits the application
     * @param e exception that occurred
     */
    private void showErrorAndExit(Exception e) {
        logger.error("{}: {}", "Failed to show login screen", e.getMessage(), e);

        // Cleanup and graceful shutdown
        cleanup();
        System.err.println("[Login][ERROR] Failed to show login screen: " + e);
        Platform.exit();
    }

    /**
     * Cleans up resources and shuts down the application
     */
    private void cleanup() {
        try {
            if (serviceLocator != null) {
                System.out.println("[Login] Shutting down service locator");
                serviceLocator.shutdown();
            }
            System.out.println("[Login] Application cleanup completed");
            logger.info("Application cleanup completed");
        } catch (Exception e) {
            System.err.println("[Login][ERROR] Error during cleanup: " + e);
            logger.error("Error during cleanup", e);
        }
    }

    /**
     * Overrides the stop method to perform cleanup before shutting down the application
     * @throws Exception if an error occurs during cleanup
     */
    @Override
    public void stop() throws Exception {
        logger.info("Application stopping...");
        System.out.println("[Login] Application stopping...");
        cleanup();
        super.stop();
    }
}