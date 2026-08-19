package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.search.SearchService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base controller class that provides common functionality for search-related controllers such as
 * SearchFormController and AdvancedSearchController that each implement the SearchFormListener interface that
 * is used to notify the parent controller when the search button is clicked and the search criteria are built to show
 * the results in the table below the search form.
 */
public abstract class BaseSearchController {
    protected static final Logger logger = LoggerFactory.getLogger(BaseSearchController.class);


    protected final SearchService searchService;
    protected final NavigationService navigationService;
    
    @FXML
    protected Label statusLabel;

    //constructor that initializes the search service and navigation service
    protected BaseSearchController() {
        this.searchService = ServiceLocator.getInstance().getSearchService();
        this.navigationService = NavigationService.getInstance();
        
        if (this.searchService == null) {
            String errorMsg = "Failed to initialize SearchService: service is null";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }
    
    protected void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    protected void showError(String title, String message) {
        UIUtils.showError(title, message);
        updateStatus("Error: " + title);
    }
    
    protected void showInfo(String title, String message) {
        UIUtils.showInfo(title, message);
    }
    
    protected Stage getStage() {
        return (Stage) (statusLabel != null ? statusLabel.getScene().getWindow() : null);
    }
}
