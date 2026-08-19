package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.rating.UserRating;
import com.papel.imdb_clone.service.rating.RatingService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for displaying detailed information about a specific content (movie or TV show) on advanced search results.
 * Handles the UI elements and data binding for the content details view.
 */
public class ContentDetailsController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ContentDetailsController.class);

    // Rating components
    @FXML private VBox ratingContainer;
    @FXML private Slider ratingSlider;
    @FXML private Label ratingValueLabel;
    @FXML private TextArea reviewTextArea;
    @FXML private Button submitRatingBtn;
    @FXML private Label ratingStatusLabel;
    
    // Services
    private final RatingService ratingService = RatingService.getInstance();
    
    // Current content and user information
    private Content content;
    private UserRating userRating;
    private boolean isRated = false;
    private int currentUserId;


    /**
     * Explicit constructor for ContentDetailsController.
     * Required for JavaFX controller initialization.
     */
    public ContentDetailsController() {
        // No initialization needed because it is a controller so it is initialized by JavaFX
    }

    /**
     * Initializes the controller with the current user ID.
     * @param currentUserId The ID of the currently authenticated user
     */
    @Override
    protected void initializeController(int currentUserId) {
        try {
            if (currentUserId <= 0) {
                throw new IllegalArgumentException("Invalid user ID: " + currentUserId);
            }
            this.currentUserId = currentUserId;
            logger.debug("Initialized controller for user ID: {}", currentUserId);
        } catch (IllegalArgumentException e) {
            String errorMsg = "Failed to initialize controller: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error during controller initialization";
            logger.error("{}: {}", errorMsg, e.getMessage(), e);
            throw new IllegalStateException(errorMsg, e);
        }
    }



    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up initial UI state and event handlers.
     */
    @FXML
    public void initialize() {
        try {
            setupRatingComponents();
            setupEventHandlers();
            logger.debug("ContentDetailsController initialized successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to initialize ContentDetailsController: " + e.getMessage();
            logger.error(errorMsg, e);
            showError("Initialization Error", "Failed to initialize the content details view. Please restart the application.");
            // Disable interactive components
            ratingContainer.setDisable(true);
            submitRatingBtn.setDisable(true);
        }
    }
    
    /**
     * Sets up the rating components and their initial state.
     */
    private void setupRatingComponents() {
        try {
            logger.debug("Setting up rating components");
            
            // Configure rating slider for decimal values (0.5 increments)
            ratingSlider.setMin(0.5);
            ratingSlider.setMax(10.0);
            ratingSlider.setMajorTickUnit(1.0);
            ratingSlider.setMinorTickCount(1);
            ratingSlider.setBlockIncrement(0.5);
            ratingSlider.setSnapToTicks(false);
            ratingSlider.setShowTickMarks(true);
            ratingSlider.setShowTickLabels(true);
            
            // Set initial value
            ratingSlider.setValue(5.0);
            ratingValueLabel.setText("5.0/10");
            
            // Update rating value label when slider changes
            ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    // Round to nearest 0.5
                    double roundedValue = Math.round(newVal.doubleValue() * 2) / 2.0;
                    ratingSlider.setValue(roundedValue);
                    ratingValueLabel.setText(String.format("%.1f/10", roundedValue));
                } catch (Exception e) {
                    logger.error("Error updating rating display: {}", e.getMessage(), e);
                    ratingValueLabel.setText("Error");
                }
            });
            
            logger.debug("Rating components set up successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to set up rating components: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }
    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        //submit rating button action
        submitRatingBtn.setOnAction(event -> handleRatingSubmission());
        //update rating value label when slider changes
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingValueLabel.setText(String.format("%.1f/10", newVal.doubleValue()));
        });
        //disable submit button if rating is 0
        submitRatingBtn.setDisable(true);
        //update submit button state when slider changes
        ratingValueLabel.setText("Please rate the content");
        //update submit button state when slider changes
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            submitRatingBtn.setDisable(newVal.doubleValue() == 0.0);
        });
    }

    
    /**
     * Handles the submission of a new or updated rating.
     */
    @FXML
    private void handleRatingSubmission() {
        try {
            logger.debug("Handling rating submission");
            
            // Validate content and user
            if (content == null) {
                throw new IllegalStateException("No content selected for rating");
            }
            
            int currentUserId = getCurrentUserId();
            // Round to nearest 0.5
            double ratingValue = Math.round(ratingSlider.getValue() * 2) / 2.0;
            String reviewText = reviewTextArea.getText();
            
            // Validate rating value
            if (ratingValue < 0.5 || ratingValue > 10.0) {
                throw new IllegalArgumentException("Rating must be between 0.5 and 10.0");
            }

            try {
                if (isRated && userRating != null) {
                    // Update existing rating
                    logger.info("Updating rating for content ID: {} by user ID: {}", content.getId(), currentUserId);
                    ratingService.updateRating(userRating.getId(), ratingValue);
                    showSuccess("Success", "Your rating has been updated!");
                } else {
                    // Create new rating
                    logger.info("Creating new rating for content ID: {} by user ID: {}", content.getId(), currentUserId);
                    userRating = ratingService.createRating(currentUserId, content.getId(), ratingValue);
                    showSuccess("Success", "Thank you for your rating!");
                    isRated = true;
                    submitRatingBtn.setText("Update Rating");
                }
                
                // Update UI on success
                ratingStatusLabel.setText("Rating saved!");
                ratingStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                
            } finally {
                // Re-enable the button regardless of success/failure
                submitRatingBtn.setDisable(false);
                ratingValueLabel.setText(String.format("%.1f/10", ratingSlider.getValue()));
            }
            
        } catch (IllegalArgumentException e) {
            String errorMsg = "Invalid input: " + e.getMessage();
            logger.warn(errorMsg, e);
            showError("Validation Error", errorMsg);
            ratingStatusLabel.setText("Please correct the errors");
            ratingStatusLabel.setStyle("-fx-text-fill: #F44336;");
        } catch (IllegalStateException e) {
            String errorMsg = "Operation not allowed: " + e.getMessage();
            logger.error(errorMsg, e);
            showError("Error", errorMsg);
            ratingStatusLabel.setText("Operation failed");
            ratingStatusLabel.setStyle("-fx-text-fill: #F44336;");
        } catch (Exception e) {
            String errorMsg = "Failed to save your rating: " + e.getMessage();
            logger.error(errorMsg, e);
            showError("Error", "An unexpected error occurred. Please try again later.");
            ratingStatusLabel.setText("Error saving rating");
            ratingStatusLabel.setStyle("-fx-text-fill: #F44336;");
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

    
    /**
     * Gets the current content object.
     * 
     * @return the content object, or null if not set
     */
    public Content getContent() {
        return content;
    }

    /**
     * Sets the content object and updates the UI accordingly.
     *
     * @param content the content to set
     */
    public void setContent(Content content) {
        this.content = content;
        
        try {
            // Check if user has already rated this content
            UserRating existingRating = ratingService.getUserRating(getCurrentUserId(), content.getId());
            if (existingRating != null) {
                this.userRating = existingRating;
                this.isRated = true;
                if (ratingSlider != null) {
                    ratingSlider.setValue(existingRating.getRating());
                }
                if (submitRatingBtn != null) {
                    submitRatingBtn.setText("Update Rating");
                }
            } else {
                this.isRated = false;
                if (submitRatingBtn != null) {
                    submitRatingBtn.setText("Submit Rating");
                }
            }
            
            logger.debug("Content set and rating status updated successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to set content: " + e.getMessage();
            logger.error(errorMsg, e);
            // Don't fail the whole UI, just log the error
            showError("Content Error", "Failed to set content. Please try again later.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialization code here if needed
        setupRatingComponents();
        setupEventHandlers();
    }
}
