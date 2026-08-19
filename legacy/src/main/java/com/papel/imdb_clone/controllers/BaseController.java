package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Base controller class providing common functionality for all controllers in the application.
 */
public abstract class BaseController implements Initializable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected DataManager dataManager;

    /**
     * Constructs a new BaseController with the specified DataManager.
     */
    protected BaseController() {
        this.dataManager = ServiceLocator.getInstance().getDataManager();
        if (this.dataManager == null) {
            throw new IllegalStateException("Failed to initialize DataManager from ServiceLocator");
        }
    }

    /**
     * Initializes the controller with the specified user ID.
     * This method serves as the main entry point for controller initialization.
     *
     * @param currentUserId The ID of the currently authenticated user, or -1 if no user is logged in
     * @throws IllegalStateException if initialization fails
     */
    public final void initialize(int currentUserId) {
        try {
            initializeController(currentUserId);
            logger.debug("{} initialized successfully for user ID: {}", 
                getClass().getSimpleName(), currentUserId);
        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize %s: %s", 
                getClass().getSimpleName(), e.getMessage());
            logger.error(errorMsg, e);
            showError("Initialization Error", "Failed to initialize the application view.");
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * Controller-specific initialization logic.
     * Subclasses should override this method to implement their specific initialization code.
     *
     * @param currentUserId The ID of the currently authenticated user
     * @throws Exception if initialization fails
     */
    protected abstract void initializeController(int currentUserId) throws Exception;


    /**
     * Displays an informational alert dialog.
     *
     * @param title   The dialog title (required)
     * @param message The message to display (required)
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected void showAlert(String title, String message) {
        validateDialogParams(title, message);
        UIUtils.showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Displays an error dialog with the specified title and message.
     * Also logs the error at WARN level.
     *
     * @param title   The dialog title (required)
     * @param message The error message to display (required)
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected void showError(String title, String message) {
        validateDialogParams(title, message);
        logger.warn("Error dialog shown - Title: {}, Message: {}", title, message);
        UIUtils.showError(title, message);
    }

    /**
     * Displays a success dialog with the specified title and message.
     *
     * @param title   The dialog title (required)
     * @param message The success message to display (required)
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected void showSuccess(String title, String message) {
        validateDialogParams(title, message);
        logger.debug("Success dialog shown - {}", message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog with Yes/No buttons.
     *
     * @param title   The dialog title (required)
     * @param message The confirmation question (required)
     * @return true if the user clicked Yes, false otherwise
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected boolean showConfirmationDialog(String title, String message) {
        validateDialogParams(title, message);
        logger.debug("Showing confirmation dialog - {}: {}", title, message);
        
        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            message,
            ButtonType.YES,
            ButtonType.NO
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }



    /**
     * Validates dialog parameters.
     *
     * @param title   The dialog title to validate
     * @param message The dialog message to validate
     * @throws IllegalArgumentException if either parameter is null or empty
     */
    private void validateDialogParams(String title, String message) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Dialog title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Dialog message cannot be null or empty");
        }
    }

    /**
     * Applies consistent styling to alert dialogs.
     *
     * @param alert The alert to style
     */
    private void styleAlert(Alert alert) {
        // Apply any global alert styling here
        DialogPane dialogPane = alert.getDialogPane();
        // Removed non-existent CSS reference
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        alert.showAndWait();
    }
    
}
