package com.papel.imdb_clone.util;

import com.papel.imdb_clone.model.content.Content;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


/**
 * Unified utility class for all UI operations including dialogs, layouts, and basic validation.
 * Combines functionality from DialogFactory, DialogUtils, and UI-related ValidationUtils methods.
 */
public final class UIUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private UIUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The primary stage of the application, used for dialog positioning.
     */
    private static Stage ownerStage;


    /**
     * Displays an error alert with the specified title and message.
     * Uses a consistent styling for error messages.
     *
     * @param title   The title of the error dialog
     * @param message The error message to display
     * @throws IllegalArgumentException if title or message is null
     */
    public static void showError(String title, String message) {
        showError(title, null, message);
    }


    /**
     * Displays an error alert with the specified title, header, and content.
     * The dialog is shown on the JavaFX Application Thread.
     *
     * @param title   The title of the error dialog
     * @param header  The header text (optional, can be null)
     * @param content The detailed error message
     * @throws IllegalArgumentException if title or content is null
     */
    public static void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = createAlert(Alert.AlertType.ERROR, title, header, content);
            alert.showAndWait();
        });
    }


    /**
     * Creates an alert with the specified type, title, header, and content.
     * The dialog is shown on the JavaFX Application Thread.
     *
     * @param type    The type of the alert
     * @param title   The title of the alert
     * @param header  The header text (optional, can be null)
     * @param content The detailed error message
     * @throws IllegalArgumentException if title or content is null
     */
    private static Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (ownerStage != null) {
            alert.initOwner(ownerStage);
        }

        // Add consistent styling
        alert.getDialogPane().setPrefWidth(400);
        alert.getDialogPane().setPrefHeight(200);

        return alert;
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = createAlert(type, title, null, message);
        alert.showAndWait();
    }

    public static void showInfo(String noResults, String s) {
        showAlert(Alert.AlertType.INFORMATION, noResults, s);
    }

}