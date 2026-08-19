package com.papel.imdb_clone.controllers.people;

import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Director;
import com.papel.imdb_clone.service.people.CelebrityService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.util.Optional;

import java.util.Arrays;
import java.util.List;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CelebritiesController implements Initializable {

    /**
     * Explicit constructor for CelebritiesController.
     * Required for JavaFX controller initialization.
     */
    public CelebritiesController() {
        logger.info("CelebritiesController constructor called");
        // Don't load data here - wait for initialize()
    }

    private static final Logger logger = LoggerFactory.getLogger(CelebritiesController.class);

    // Actor UI Components
    @FXML
    private TableView<Actor> actorsTable;
    @FXML
    private TableColumn<Actor, String> actorNameColumn;
    @FXML
    private TableColumn<Actor, String> actorBirthDateColumn;
    @FXML
    private TableColumn<Actor, String> actorGenderColumn;
    @FXML
    private TableColumn<Actor, String> actorNationalityColumn;
    @FXML
    private TableColumn<Actor, String> actorNotableWorksColumn;
    @FXML
    private TextField actorSearchField;
    @FXML
    private Label statusLabel;
    
    // Edit and Delete Buttons
    @FXML private Button editActorButton;
    @FXML private Button deleteActorButton;
    @FXML private Button editDirectorButton;
    @FXML private Button deleteDirectorButton;
    

    // Director UI Components
    @FXML
    private TableView<Director> directorsTable;
    @FXML
    private TableColumn<Director, String> directorNameColumn;
    @FXML
    private TableColumn<Director, String> directorBirthDateColumn;
    @FXML
    private TableColumn<Director, String> directorGenderColumn;
    @FXML
    private TableColumn<Director, String> directorNationalityColumn;
    @FXML
    private TableColumn<Director, String> directorNotableWorksColumn;
    @FXML
    private TextField directorSearchField;

    @FXML
    private TextField unifiedSearchField;

    // Data
    private final ObservableList<Actor> actors = FXCollections.observableArrayList();
    private final ObservableList<Director> directors = FXCollections.observableArrayList();
    private final FilteredList<Actor> filteredActors = new FilteredList<>(actors);
    private final FilteredList<Director> filteredDirectors = new FilteredList<>(directors);

    // Services
    private CelebrityService<Actor> actorService;
    private CelebrityService<Director> directorService;
    private Map<String, Object> data;

    /**
     * Handles navigation to the home view.
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void goToHome(MouseEvent event) {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/home/home-view.fxml", null, null, "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home: ", e);
            showError("Navigation Error", "Failed to navigate to home view.");
        }
    }

    //initialize the controller
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up table selection listeners for enabling/disabling edit/delete buttons
        actorsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean disabled = newSelection == null;
            editActorButton.setDisable(disabled);
            deleteActorButton.setDisable(disabled);
        });
        // Set up table selection listeners for enabling/disabling edit/delete buttons
        directorsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean disabled = newSelection == null;
            editDirectorButton.setDisable(disabled);
            deleteDirectorButton.setDisable(disabled);
        });

        // Set up table selection listeners for enabling/disabling edit/delete buttons
        actorsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean disabled = newSelection == null;
            editActorButton.setDisable(disabled);
            deleteActorButton.setDisable(disabled);
        });
        // Set up table selection listeners for enabling/disabling edit/delete buttons
        directorsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean disabled = newSelection == null;
            editDirectorButton.setDisable(disabled);
            deleteDirectorButton.setDisable(disabled);
        });
        
        logger.info("Initializing CelebritiesController...");
            
        // Get DataManager instance and ensure it's initialized
        DataManager dataManager = DataManager.getInstance();
        if (dataManager == null) {
            throw new IllegalStateException("DataManager is not properly initialized");
        }
            
        // Ensure services are registered
        dataManager.registerServices();
            
        // Get services from DataManager
        actorService = dataManager.getActorService();
        directorService = dataManager.getDirectorService();
            
        if (actorService == null || directorService == null) {
            throw new IllegalStateException("Failed to initialize required services. ActorService: " + 
                (actorService != null ? "OK" : "null") + 
                ", DirectorService: " + 
                (directorService != null ? "OK" : "null"));
        }

        // Initialize UI components
        try {
            initializeActorTable();
            initializeDirectorTable();
            initializeUnifiedSearch();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UI components: " + e.getMessage(), e);
        }

        // Load initial data in a separate thread to keep UI responsive
        new Thread(this::loadCelebrities).start();
            
        logger.info("CelebritiesController initialized successfully");
            
    }

    private void initializeActorTable() {
        try {
            // Set up cell value factory for actor name
            actorNameColumn.setCellValueFactory(cellData -> {
                try {
                    Actor actor = cellData.getValue();
                    if (actor != null) {
                        String firstName = actor.getFirstName() != null ? actor.getFirstName() : "";
                        String lastName = actor.getLastName() != null ? actor.getLastName() : "";
                        logger.debug("Actor name for actor {} {}: {}",
                                actor.getFirstName(), actor.getLastName(),
                                String.format("%s %s", firstName, lastName).trim());
                        return new SimpleStringProperty(String.format("%s %s", firstName, lastName).trim());
                    }
                } catch (Exception e) {
                    logger.error("Error getting actor name: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for birthdate with proper formatting
            actorBirthDateColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null && cellData.getValue().getBirthDate() != null) {
                        // Format the date as yyyy-MM-dd for consistency
                        String birthDate = cellData.getValue().getBirthDate().toString();
                        logger.debug("Birth date for actor {} {}: {}",
                                cellData.getValue().getFirstName(), cellData.getValue().getLastName(),
                                cellData.getValue().getBirthDate().toString());
                        return new SimpleStringProperty(cellData.getValue().getBirthDate().toString());
                    }
                } catch (Exception e) {
                    logger.error("Error getting birth date: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Gender column with proper handling
            actorGenderColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null) {
                        char gender = cellData.getValue().getGender();
                        if (gender == 'M' || gender == 'm') {
                            return new SimpleStringProperty("Male");
                        } else if (gender == 'F' || gender == 'f') {
                            return new SimpleStringProperty("Female");
                        } else {
                            return new SimpleStringProperty("Other");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting gender: {}", e.getMessage());
                }
                // Log error and show error message
                return new SimpleStringProperty("N/A");
            });

            // Nationality/Ethnicity column with proper handling
            actorNationalityColumn.setCellValueFactory(cellData -> {
                try {
                    Actor actor = cellData.getValue();
                    if (actor != null) {
                        Ethnicity ethnicity = actor.getEthnicity();
                        if (ethnicity != null) {
                            return new SimpleStringProperty(ethnicity.getLabel());
                        } else if (actor.getNationality() != null) {
                            return new SimpleStringProperty(actor.getNationality().getLabel());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting nationality/ethnicity: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Notable works column with improved formatting and null safety
            actorNotableWorksColumn.setCellValueFactory(cellData -> {
                try {
                    Actor actor = cellData.getValue();
                    if (actor != null) {
                        try {
                            // Safely get notable works with null check
                            List<String> notableWorks = actor.getNotableWorks();
                            if (notableWorks != null && !notableWorks.isEmpty()) {
                                // Filter out any null or empty strings from notable works
                                List<String> validWorks = notableWorks.stream()
                                        .filter(work -> work != null && !work.trim().isEmpty())
                                        .collect(Collectors.toList());
                                
                                logger.debug("Notable works for actor {}: {}", 
                                    actor.getFirstName() + " " + actor.getLastName(), 
                                    validWorks.isEmpty() ? "No works" : validWorks);

                                // If there are valid works, format them
                                if (!validWorks.isEmpty()) {
                                    // Format the notable works as a comma-separated list, limit to 4 items for display
                                    int maxWorks = Math.min(4, validWorks.size());
                                    String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                    if (validWorks.size() > 4) {
                                        worksText += "..";
                                    }
                                    return new SimpleStringProperty(worksText);
                                }
                            }
                            return new SimpleStringProperty("No works");
                        } catch (Exception e) {
                            logger.warn("Error processing notable works for {} {}: {}",
                                    actor.getFirstName(), actor.getLastName(), e.getMessage(), e);
                            return new SimpleStringProperty("Error loading works");
                        }
                    }
                    return new SimpleStringProperty("");
                } catch (Exception e) {
                    logger.error("Unexpected error in notable works cell factory: {}", e.getMessage(), e);
                    return new SimpleStringProperty("Error");
                }
            });

            // Set a tooltip to show all notable works on hover
            actorNotableWorksColumn.setCellFactory(column -> {
                return new TableCell<Actor, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);

                            // Get the list of notable works for the tooltip (max 7)
                            Actor actor = getTableView().getItems().get(getIndex());
                            if (actor != null) {
                                List<String> allWorks = actor.getNotableWorks();
                                if (allWorks != null && !allWorks.isEmpty()) {
                                    // Limit to first 7 works for the tooltip
                                    int maxWorks = Math.min(7, allWorks.size());
                                    List<String> limitedWorks = allWorks.subList(0, maxWorks);
                                    String tooltipText = String.join("\n• ", limitedWorks);
                                    
                                    // Add a note if there are more works than shown
                                    if (allWorks.size() > 7) {
                                        tooltipText += "\n• ... and " + (allWorks.size() - 7) + " more";
                                    }
                                    
                                    setTooltip(new Tooltip("• " + tooltipText));
                                } else {
                                    setTooltip(new Tooltip("No notable works available"));
                                }
                            }
                        }
                    }
                };
            });

            // Set the items to the table
            actorsTable.setItems(filteredActors);

            // Enable sorting on all columns
            actorNameColumn.setSortable(true);
            actorBirthDateColumn.setSortable(true);
            actorGenderColumn.setSortable(true);
            actorNationalityColumn.setSortable(true);
            actorNotableWorksColumn.setSortable(true);
            actorNotableWorksColumn.setSortType(TableColumn.SortType.ASCENDING);

        } catch (Exception e) {
            logger.error("Error initializing actor table: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize actor table: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToHome() {
        try {
            NavigationService.getInstance().navigateTo("/fxml/base/home-view.fxml",
                    data, (Stage) actorsTable.getScene().getWindow(), "Home");
        } catch (Exception e) {
            logger.error("Error navigating to home: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to navigate to home: " + e.getMessage());
        }
    }
    
    /**
     * Handles the edit actor button click.
     */
    @FXML
    private void handleEditActor() {
        Actor selectedActor = actorsTable.getSelectionModel().getSelectedItem();
        if (selectedActor != null) {
            showAddActorDialog(selectedActor);
        }
    }
    
    /**
     * Handles the delete actor button click.
     */
    @FXML
    private void handleDeleteActor() {
        Actor selectedActor = actorsTable.getSelectionModel().getSelectedItem();
        if (selectedActor != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Actor");
            alert.setContentText("Are you sure you want to delete " + selectedActor.getFullName() + "?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Remove from the service/database first
                    boolean deleted = actorService.delete(selectedActor.getId());
                    
                    if (deleted) {
                        // Remove from the underlying observable list
                        // This will automatically update the filtered list and table
                        actors.removeIf(actor -> actor.getId() == selectedActor.getId());

                        // Clear selection
                        actorsTable.getSelectionModel().clearSelection();
                        
                        // Update status
                        statusLabel.setText("Successfully deleted actor: " + selectedActor.getFullName());
                        updateStatusLabel();
                    } else {
                        throw new Exception("Failed to delete actor from the database");
                    }
                } catch (Exception e) {
                    logger.error("Error deleting actor", e);
                    statusLabel.setText("Error deleting actor: " + e.getMessage());
                    
                    // Reload the data to ensure consistency
                    loadCelebrities();
                }
            }
        }
    }
    
    /**
     * Handles the edit director button click.
     */
    @FXML
    private void handleEditDirector() {
        Director selectedDirector = directorsTable.getSelectionModel().getSelectedItem();
        if (selectedDirector != null) {
            showAddDirectorDialog(selectedDirector);
        }
    }

    /**
     * Handles the delete director button click.
     */
    @FXML
    private void handleDeleteDirector() {
        Director selectedDirector = directorsTable.getSelectionModel().getSelectedItem();
        if (selectedDirector != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Director");
            alert.setContentText("Are you sure you want to delete " + selectedDirector.getFullName() + "?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Create a new modifiable list
                    List<Director> currentDirectors = new ArrayList<>(directors);
                    
                    // Remove the director from our modifiable list
                    if (currentDirectors.remove(selectedDirector)) {
                        // Update the observable list with the new list
                        directors.setAll(currentDirectors);
                        
                        // Update the table items
                        directorsTable.setItems(directors);
                        
                        // Show success message
                        statusLabel.setText("Successfully deleted director: " + selectedDirector.getFullName());
                    } else {
                        throw new IllegalStateException("Director not found in the list");
                    }
                } catch (Exception e) {
                    logger.error("Error deleting director", e);
                    statusLabel.setText("Error deleting director: " + e.getMessage());
                    
                    // Reload the data to ensure consistency
                    loadCelebrities();
                }
            }
        }
    }

    /**
     * Handles the refresh button action to reload all celebrities.
     * This method clears all search fields and reloads both actors and directors.
     * 
     * @param event The action event that triggered the refresh
     */
    @FXML
    public void handleRefresh(javafx.event.ActionEvent event) {
        try {
            logger.info("Starting refresh of all celebrity data...");

            // Clear search fields on the JavaFX Application Thread
            Platform.runLater(() -> {
                if (unifiedSearchField != null) {
                    unifiedSearchField.clear();
                }
                if (actorSearchField != null) {
                    actorSearchField.clear();
                }
                if (directorSearchField != null) {
                    directorSearchField.clear();
                }
            });

            // Clear current data
            actors.clear();
            directors.clear();
            filteredActors.clear();
            filteredDirectors.clear();

            // Reload all celebrities in a background thread
            new Thread(() -> {
                try {
                    // Load fresh data
                    loadCelebrities();
                    
                    // Update UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            if (actorsTable != null) {
                                actorsTable.refresh();
                            }
                            if (directorsTable != null) {
                                directorsTable.refresh();
                            }
                            
                            // Show success message
                            String successMessage = "Celebrities list has been refreshed.";
                            if (statusLabel != null) {
                                statusLabel.setText(successMessage);
                                logger.info(successMessage);
                                
                                // Clear the status message after 3 seconds
                                new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            Platform.runLater(() -> {
                                                if (statusLabel != null) {
                                                    statusLabel.setText("");
                                                }
                                            });
                                        }
                                    },
                                    3000
                                );
                            }
                        } catch (Exception e) {
                            logger.error("Error updating UI after refresh", e);
                            showError("UI Update Error", "Failed to update UI after refresh.");
                        }
                    });
                } catch (Exception e) {
                    logger.error("Error refreshing celebrities", e);
                    Platform.runLater(() -> 
                        showError("Refresh Error", "Failed to refresh celebrities. Please try again.")
                    );
                }
            }).start();
        } catch (Exception e) {
            String errorMsg = "Failed to refresh celebrities: " + e.getMessage();
            logger.error(errorMsg, e);
            showError("Refresh Error", errorMsg);
        }
    }

    //initialize director table
    private void initializeDirectorTable() {
        try {
            // Set up cell value factory for director name
            directorNameColumn.setCellValueFactory(cellData -> {
                try {
                    Director director = cellData.getValue();
                    if (director != null) {
                        String firstName = director.getFirstName() != null ? director.getFirstName() : "";
                        String lastName = director.getLastName() != null ? director.getLastName() : "";
                        String fullName = String.format("%s %s", firstName, lastName).trim();
                        logger.debug("Director name: {}", fullName);
                        return new SimpleStringProperty(fullName);
                    }
                } catch (Exception e) {
                    logger.error("Error getting director name: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for birth date with proper formatting
            directorBirthDateColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null) {
                        LocalDate birthDate = cellData.getValue().getBirthDate();
                        if (birthDate != null) {
                            String formattedDate = birthDate.toString();
                            logger.debug("Director birth date: {}", formattedDate);
                            return new SimpleStringProperty(formattedDate);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting director birth date: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for gender
            directorGenderColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null) {
                        char gender = cellData.getValue().getGender();
                        String genderStr = "Unknown";
                        if (gender == 'M' || gender == 'm') {
                            genderStr = "Male";
                        } else if (gender == 'F' || gender == 'f') {
                            genderStr = "Female";
                        }
                        logger.debug("Director gender: {}", genderStr);
                        return new SimpleStringProperty(genderStr);
                    }
                } catch (Exception e) {
                    logger.error("Error getting director gender: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for nationality/ethnicity
            directorNationalityColumn.setCellValueFactory(cellData -> {
                try {
                    Director director = cellData.getValue();
                    if (director != null) {
                        Ethnicity ethnicity = director.getEthnicity();
                        if (ethnicity != null) {
                            logger.debug("Director {} {} nationality: {}", 
                                director.getFirstName(), director.getLastName(), 
                                ethnicity.getLabel());
                            return new SimpleStringProperty(ethnicity.getLabel());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting director nationality: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for notable works with improved formatting and null safety
            directorNotableWorksColumn.setCellValueFactory(cellData -> {
                try {
                    Director director = cellData.getValue();
                    if (director != null) {
                        try {
                            // Safely get notable works with null check
                            List<String> notableWorks = director.getNotableWorks();
                            if (notableWorks != null && !notableWorks.isEmpty()) {
                                // Filter out any null or empty strings from notable works
                                List<String> validWorks = notableWorks.stream()
                                    .filter(work -> work != null && !work.trim().isEmpty())
                                    .collect(Collectors.toList());
                                
                                logger.debug("Notable works for director {}: {}", 
                                    director.getFirstName() + " " + director.getLastName(), 
                                    validWorks.isEmpty() ? "No works" : validWorks);

                                // If there are valid works, format them
                                if (!validWorks.isEmpty()) {
                                    // Format the notable works as a comma-separated list, limit to 4 items for display
                                    int maxWorks = Math.min(4, validWorks.size());
                                    String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                    if (validWorks.size() > 4) {
                                        worksText += "..";
                                    }
                                    return new SimpleStringProperty(worksText);
                                }
                            }
                            return new SimpleStringProperty("No works");
                        } catch (Exception e) {
                            logger.warn("Error processing notable works for director {} {}: {}",
                                director.getFirstName(), director.getLastName(), e.getMessage(), e);
                            return new SimpleStringProperty("Error loading works");
                        }
                    }
                    return new SimpleStringProperty("");
                } catch (Exception e) {
                    logger.error("Unexpected error in director notable works cell factory: {}", e.getMessage(), e);
                    return new SimpleStringProperty("Error");
                }
            });

            // Add tooltip for notable works to show full list on hover
            directorNotableWorksColumn.setCellFactory(column -> {
                return new TableCell<Director, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);
                            Director director = getTableView().getItems().get(getIndex());
                            if (director != null) {
                                List<String> allWorks = director.getNotableWorks();
                                if (allWorks != null && !allWorks.isEmpty()) {
                                    // Limit to first 6 works for the tooltip
                                    int maxWorks = Math.min(6, allWorks.size());
                                    List<String> limitedWorks = allWorks.subList(0, maxWorks);
                                    String tooltipText = String.join("\n• ", limitedWorks);
                                    
                                    // Add a note if there are more works than shown
                                    if (allWorks.size() > 6) {
                                        tooltipText += "\n• ... and " + (allWorks.size() - 6) + " more";
                                    }
                                    
                                    setTooltip(new Tooltip("• " + tooltipText));
                                }
                            }
                        }
                    }
                };
            });

            // Set the items to the table
            directorsTable.setItems(filteredDirectors);

            // Enable sorting on all columns
            directorNameColumn.setSortable(true);
            directorBirthDateColumn.setSortable(true);
            directorGenderColumn.setSortable(true);
            directorNationalityColumn.setSortable(true);
            directorNotableWorksColumn.setSortable(true);

            logger.info("Director table initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing director table: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize director table: " + e.getMessage());
        }

        // Set up cell value factory for gender with consistent handling
        directorGenderColumn.setCellValueFactory(cellData -> {
            try {
                if (cellData.getValue() != null) {
                    char gender = cellData.getValue().getGender();
                    if (gender == 'M' || gender == 'm') {
                        return new SimpleStringProperty("Male");
                    } else if (gender == 'F' || gender == 'f') {
                        return new SimpleStringProperty("Female");
                    } else {
                        return new SimpleStringProperty("Other");
                    }
                }
            } catch (Exception e) {
                logger.error("Error getting director gender: {}", e.getMessage());
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up cell value factory for nationality/ethnicity
        directorNationalityColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    Ethnicity ethnicity = director.getEthnicity();
                    if (ethnicity != null) {
                        return new SimpleStringProperty(ethnicity.getLabel());
                    } else if (director.getNationality() != null) {
                        return new SimpleStringProperty(director.getNationality().getLabel());
                    }
                }
            } catch (Exception e) {
                logger.error("Error getting director nationality: {}", e.getMessage());
                showError("Error", "Failed to get director nationality: " + e.getMessage());
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up cell value factory for notable works with proper formatting
        directorNotableWorksColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    try {
                        List<String> notableWorks = director.getNotableWorks();
                        if (notableWorks != null && !notableWorks.isEmpty()) {
                            // Filter out any null or empty strings from notable works
                            List<String> validWorks = notableWorks.stream()
                                    .filter(work -> work != null && !work.trim().isEmpty())
                                    .collect(Collectors.toList());

                            if (!validWorks.isEmpty()) {
                                // Format the notable works as a comma-separated list, limit to 4 items for display
                                int maxWorks = Math.min(4, validWorks.size());
                                String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                if (validWorks.size() > 4) {
                                    // Add ellipsis if there are more than 4 works
                                    worksText += "...";
                                }
                                logger.debug("Notable works for director {} {}: {}",
                                        director.getFirstName(), director.getLastName(), worksText);
                                return new SimpleStringProperty(worksText);
                            }
                        }
                        // If we get here, there are no notable works
                        logger.debug("No notable works found for director {} {}",
                                director.getFirstName(), director.getLastName());
                        return new SimpleStringProperty("-"); // Use dash for empty works
                    } catch (Exception e) {
                        logger.warn("Error processing notable works for director {} {}: {}",
                                director.getFirstName(), director.getLastName(), e.getMessage());
                        return new SimpleStringProperty("Error loading works");
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error getting director notable works: {}", e.getMessage(), e);
            }
            return new SimpleStringProperty("-"); // Default fallback
        });

        // Set a tooltip to show all notable works on hover
        directorNotableWorksColumn.setCellFactory(column -> {
            return new TableCell<Director, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);

                        // Get the full list of notable works for the tooltip
                        Director director = getTableView().getItems().get(getIndex());
                        if (director != null) {
                            List<String> allWorks = director.getNotableWorks();
                            if (allWorks != null && !allWorks.isEmpty()) {
                                String tooltipText = String.join("\n• ", allWorks);
                                setTooltip(new Tooltip("• " + tooltipText));
                            } else {
                                setTooltip(new Tooltip("No notable works available"));
                            }
                        }
                    }
                }
            };
        });

        // Set the items to the table
        directorsTable.setItems(filteredDirectors);

        // Enable sorting on all columns
        directorNameColumn.setSortable(true);
        logger.info("Starting to load celebrities...");
        
        // Initialize empty lists to avoid NPEs in case of errors
        List<Actor> actorList = Collections.emptyList();
        List<Director> directorList = Collections.emptyList();
        
        try {
            // Load actors with notable works
            actorList = actorService.getAll();
            logger.info("Loaded {} actors", actorList.size());
            
            // Log notable works for first few actors
            int actorsToLog = Math.min(5, actorList.size());
            for (int i = 0; i < actorsToLog; i++) {
                Actor actor = actorList.get(i);
                logger.debug("Actor[{}]: {} {}", i, actor.getFirstName(), actor.getLastName());
                logger.debug("  Notable Works: {}", 
                    actor.getNotableWorks() != null && !actor.getNotableWorks().isEmpty() ? 
                    String.join(", ", actor.getNotableWorks()) : "None");
            }

            // Load directors with notable works
            directorList = directorService.getAll();
            logger.info("Loaded {} directors", directorList.size());
            
            // Log notable works for first few directors
            int directorsToLog = Math.min(5, directorList.size());

            for (int i = 0; i < directorsToLog; i++) {
                Director director = directorList.get(i);
                logger.debug("Director[{}]: {} {}", i, director.getFirstName(), director.getLastName());
                logger.debug("  Notable Works: {}", 
                    director.getNotableWorks() != null && !director.getNotableWorks().isEmpty() ? 
                    String.join(", ", director.getNotableWorks()) : "None");
            }
            
            // Update UI on the JavaFX Application Thread
            updateUIWithCelebrities(actorList, directorList);
            
        } catch (Exception e) {
            String errorMsg = "Failed to load celebrities: " + e.getMessage();
            logger.error(errorMsg, e);
            
            // Update UI with empty lists in case of error
            updateUIWithCelebrities(Collections.emptyList(), Collections.emptyList());
            
            // Show error to user
            Platform.runLater(() -> 
                showError("Load Error", "Failed to load celebrities. Please try again later.")
            );
        }
        
        logger.info("Finished loading celebrities");
    }
    
    /**
     * Updates the UI with the loaded celebrities' data.
     * This method is called on the JavaFX Application Thread.
     * 
     * @param actorList The list of actors to display
     * @param directorList The list of directors to display
     */
    private void updateUIWithCelebrities(List<Actor> actorList, List<Director> directorList) {
        Platform.runLater(() -> {
            try {
                // Set the actors and directors to the ObservableLists
                actors.setAll(actorList);
                directors.setAll(directorList);
                
                // Reset filters
                filteredActors.setPredicate(actor -> true);
                filteredDirectors.setPredicate(director -> true);
                
                // Update status with success message
                if (statusLabel != null) {
                    String status = String.format("Loaded %d actors and %d directors", 
                        actorList.size(), directorList.size());
                    statusLabel.setText(status);
                    logger.info(status);
                    
                    // Clear the status message after 3 seconds
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    if (statusLabel != null) {
                                        statusLabel.setText("");
                                    }
                                });
                            }
                        },
                        3000
                    );
                }
            } catch (Exception e) {
                logger.error("Error updating UI with loaded celebrities: {}", e.getMessage(), e);
                showError("UI Update Error", "Failed to update UI with loaded data: " + e.getMessage());
            }
        });
    }

/**
 * Displays an error dialog with the specified title and message.
 * This method is thread-safe and can be called from any thread.
 * 
 * @param title The title of the error dialog
 * @param message The error message to display
 */
private void showError(String title, String message) {
    Platform.runLater(() -> {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Add a copy button to the dialog
            alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Show the dialog and wait for it to be closed
            alert.showAndWait();

            // Log the error
            logger.error("Error Dialog - {}: {}", title, message);
        } catch (Exception e) {
            // If there's an error showing the dialog, log it to the console
            System.err.println("Failed to show error dialog: " + e.getMessage());
        }
    });
}


/**
 * Handles the "Add Celebrity" button click event.
 * Shows a dialog to choose between adding an actor, director, or both.
 */
@FXML
private void handleAddCelebrity() {
    try {
        // Create a choice dialog for selecting celebrity type
        List<String> choices = Arrays.asList("Actor", "Director", "Both");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Actor", choices);
        dialog.setTitle("Add New Celebrity");
        dialog.setHeaderText("Select Celebrity Type");
        dialog.setContentText("Choose type:");

        // Customize the dialog buttons
        ButtonType addButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(addButton, ButtonType.CANCEL);

        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String choice = result.get();
            switch (choice) {
                case "Actor":
                    showAddActorDialog(null);
                    break;
                case "Director":
                    showAddDirectorDialog(null);
                    break;
                case "Both":
                    showAddCelebrityDialog();
                    break;
            }
        }
    } catch (Exception e) {
        logger.error("Error in handleAddCelebrity: {}", e.getMessage(), e);
        showError("Error", "Failed to open add celebrity dialog: " + e.getMessage());
    }
}

/**
 * Shows a dialog for adding or editing an actor.
 * @param actorToEdit The actor to edit, or null to add a new actor
 */
private void showAddActorDialog(Actor actorToEdit) {
    try {
        Dialog<Actor> dialog = new Dialog<>();
        dialog.setTitle(actorToEdit != null ? "Edit Actor" : "Add New Actor");
        dialog.setHeaderText("Enter actor details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Birth Date");
        ComboBox<String> genderComboBox = new ComboBox<>(FXCollections.observableArrayList("M", "F", "O"));
        genderComboBox.setPromptText("Gender (M/F/O)");

        // Add ethnicity combo box
        ComboBox<Ethnicity> ethnicityComboBox = new ComboBox<>();
        ethnicityComboBox.getItems().addAll(Ethnicity.values());
        ethnicityComboBox.setPromptText("Select Ethnicity");

        TextArea notableWorksArea = new TextArea();
        notableWorksArea.setPromptText("Enter notable works, separated by commas");
        notableWorksArea.setPrefRowCount(3);

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Birth Date:"), 0, 2);
        grid.add(birthDatePicker, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(genderComboBox, 1, 3);
        grid.add(new Label("Ethnicity:"), 0, 4);
        grid.add(ethnicityComboBox, 1, 4);
        grid.add(new Label("Notable Works:"), 0, 5);
        grid.add(notableWorksArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the first name field by default
        Platform.runLater(firstNameField::requestFocus);

        // Convert the result to an Actor when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    LocalDate birthDate = birthDatePicker.getValue();
                    char gender = genderComboBox.getValue() != null ? genderComboBox.getValue().charAt(0) : 'U';
                    Ethnicity ethnicity = ethnicityComboBox.getValue() != null ? ethnicityComboBox.getValue() : Ethnicity.UNKNOWN;

                    // Parse notable works
                    String notableWorks = Arrays.stream(notableWorksArea.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining(", "));

                    // Create actor with required fields
                    Actor actor = new Actor(firstName, lastName, birthDate, gender, ethnicity);
                    actor.setNotableWorks(notableWorks);

                    return actor;
                } catch (Exception e) {
                    showError("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<Actor> result = dialog.showAndWait();
        result.ifPresent(actor -> {
            try {
                actorService.save(actor);
                actors.add(actor);
                updateStatus("Actor added successfully!");
            } catch (Exception e) {
                showError("Error", "Failed to add actor: " + e.getMessage());
            }
        });
    } catch (Exception e) {
        logger.error("Error in showAddActorDialog: {}", e.getMessage(), e);
        showError("Error", "Failed to open add actor dialog: " + e.getMessage());
    }
}

    /**
     * Shows a dialog for adding a celebrity with both actor and director roles.
     */
    private void showAddCelebrityDialog() {
        try {
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Add New Celebrity");
            dialog.setHeaderText("Enter celebrity details (will be added as both Actor and Director)");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField firstNameField = new TextField();
            firstNameField.setPromptText("First Name");
            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Last Name");
            DatePicker birthDatePicker = new DatePicker();
            birthDatePicker.setPromptText("Birth Date");
            ComboBox<String> genderComboBox = new ComboBox<>(FXCollections.observableArrayList("M", "F", "O"));
            genderComboBox.setPromptText("Gender (M/F/O)");

            // Add ethnicity combo box
            ComboBox<Ethnicity> ethnicityComboBox = new ComboBox<>();
            ethnicityComboBox.getItems().addAll(Ethnicity.values());
            ethnicityComboBox.setPromptText("Select Ethnicity");

            TextArea notableWorksArea = new TextArea();
            notableWorksArea.setPromptText("Enter notable works, separated by commas");
            notableWorksArea.setPrefRowCount(3);

            grid.add(new Label("First Name:"), 0, 0);
            grid.add(firstNameField, 1, 0);
            grid.add(new Label("Last Name:"), 0, 1);
            grid.add(lastNameField, 1, 1);
            grid.add(new Label("Birth Date:"), 0, 2);
            grid.add(birthDatePicker, 1, 2);
            grid.add(new Label("Gender:"), 0, 3);
            grid.add(genderComboBox, 1, 3);
            grid.add(new Label("Ethnicity:"), 0, 4);
            grid.add(ethnicityComboBox, 1, 4);
            grid.add(new Label("Notable Works:"), 0, 5);
            grid.add(notableWorksArea, 1, 5);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the first name field by default
            Platform.runLater(firstNameField::requestFocus);

            // Convert the result to a map when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String firstName = firstNameField.getText().trim();
                        String lastName = lastNameField.getText().trim();
                        LocalDate birthDate = birthDatePicker.getValue();
                        char gender = genderComboBox.getValue() != null ? genderComboBox.getValue().charAt(0) : 'U';
                        Ethnicity ethnicity = ethnicityComboBox.getValue() != null ? ethnicityComboBox.getValue() : Ethnicity.UNKNOWN;

                        // Parse notable works
                        String notableWorks = Arrays.stream(notableWorksArea.getText().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.joining(", "));

                        // Create a map to return all the values
                        Map<String, Object> result = new HashMap<>();
                        result.put("firstName", firstName);
                        result.put("lastName", lastName);
                        result.put("birthDate", birthDate);
                        result.put("gender", gender);
                        result.put("ethnicity", ethnicity);
                        result.put("notableWorks", notableWorks);

                        return result;
                    } catch (Exception e) {
                        showError("Error", "Invalid input: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(celebrityData -> {
                try {
                    // Extract data from the map
                    String firstName = (String) celebrityData.get("firstName");
                    String lastName = (String) celebrityData.get("lastName");
                    
                    // Validate required fields
                    if (lastName == null || lastName.trim().isEmpty()) {
                        showError("Validation Error", "Last name is required");
                        return;
                    }
                    
                    LocalDate birthDate = (LocalDate) celebrityData.get("birthDate");
                    char gender = (char) celebrityData.get("gender");
                    Ethnicity ethnicity = (Ethnicity) celebrityData.get("ethnicity");
                    String notableWorks = (String) celebrityData.get("notableWorks");

                    // Create and save actor
                    Actor actor = new Actor(
                        firstName != null ? firstName.trim() : "",
                        lastName.trim(),  // We know this is not null or empty
                        birthDate,
                        gender,
                        ethnicity
                    );

                    actor.setNotableWorks(notableWorks != null ? notableWorks : "");
                    actorService.save(actor);
                    actors.add(actor);

                    // Create and save director
                    Director director = Director.getInstance(
                        firstName != null ? firstName.trim() : "",
                        lastName.trim(),  // We know this is not null or empty
                        birthDate,
                        gender,
                        ethnicity
                    );
                    director.setNotableWorks(notableWorks != null ? notableWorks : "");
                    directorService.save(director);
                    directors.add(director);

                    updateStatus("Celebrity added successfully as both Actor and Director!");
                } catch (Exception e) {
                    logger.error("Error saving celebrity: {}", e.getMessage(), e);
                    showError("Error", "Failed to save celebrity: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error in showAddCelebrityDialog: {}", e.getMessage(), e);
            showError("Error", "Failed to open add celebrity dialog: " + e.getMessage());
        }
    }

    /**
     * Shows a dialog for adding a new director or editing an existing one.
     * @param directorToEdit The director to edit, or null to add a new director
     */
    private void showAddDirectorDialog(Director directorToEdit) {
        try {
            // Create a dialog for adding a new director
            Dialog<Director> dialog = new Dialog<>();
            dialog.setTitle(directorToEdit != null ? "Edit Director" : "Add New Director");
            dialog.setHeaderText("Enter director details");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField firstNameField = new TextField();
            firstNameField.setPromptText("First Name");
            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Last Name");
            DatePicker birthDatePicker = new DatePicker();
            birthDatePicker.setPromptText("Birth Date");
            ComboBox<String> genderComboBox = new ComboBox<>(FXCollections.observableArrayList("M", "F", "O"));
            genderComboBox.setPromptText("Gender (M/F/O)");

            // Add ethnicity combo box
            ComboBox<Ethnicity> ethnicityComboBox = new ComboBox<>();
            ethnicityComboBox.getItems().addAll(Ethnicity.values());
            ethnicityComboBox.setPromptText("Select Ethnicity");

            TextArea notableWorksArea = new TextArea();
            notableWorksArea.setPromptText("Enter notable works, separated by commas");
            notableWorksArea.setPrefRowCount(3);

            grid.add(new Label("First Name:"), 0, 0);
            grid.add(firstNameField, 1, 0);
            grid.add(new Label("Last Name:"), 0, 1);
            grid.add(lastNameField, 1, 1);
            grid.add(new Label("Birth Date:"), 0, 2);
            grid.add(birthDatePicker, 1, 2);
            grid.add(new Label("Gender:"), 0, 3);
            grid.add(genderComboBox, 1, 3);
            grid.add(new Label("Ethnicity:"), 0, 4);
            grid.add(ethnicityComboBox, 1, 4);
            grid.add(new Label("Notable Works:"), 0, 5);
            grid.add(notableWorksArea, 1, 5);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the first name field by default
            Platform.runLater(firstNameField::requestFocus);

            // Convert the result to a Director when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String firstName = firstNameField.getText().trim();
                        String lastName = lastNameField.getText().trim();
                        LocalDate birthDate = birthDatePicker.getValue();
                        char gender = genderComboBox.getValue() != null ? genderComboBox.getValue().charAt(0) : 'U';
                        Ethnicity ethnicity = ethnicityComboBox.getValue() != null ? ethnicityComboBox.getValue() : Ethnicity.UNKNOWN;

                        // Parse notable works
                        String notableWorks = Arrays.stream(notableWorksArea.getText().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.joining(", "));

                        // Create director with required fields using factory method
                        Director director = Director.getInstance(firstName, lastName, birthDate, gender, ethnicity);
                        director.setNotableWorks(notableWorks);

                        return director;
                    } catch (Exception e) {
                        showError("Error", "Invalid input: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<Director> result = dialog.showAndWait();
            result.ifPresent(director -> {
                try {
                    directorService.save(director);
                    directors.add(director);
                    updateStatus("Director added successfully!");
                } catch (Exception e) {
                    showError("Error", "Failed to add director: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error in showAddDirectorDialog: {}", e.getMessage(), e);
            showError("Error", "Failed to open add director dialog: " + e.getMessage());
        }
    }

    /**
     * Initializes the unified search functionality for both actors and directors.
     */
    private void initializeUnifiedSearch() {
        // Unified search field
        if (unifiedSearchField != null) {
            unifiedSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterActors(newValue);
                filterDirectors(newValue);
                updateStatusLabel();
            });
        }
        
        // actor search field
        if (actorSearchField != null) {
            actorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterActors(newValue);
                updateStatusLabel();
            });
        }

        //director search field
        if (directorSearchField != null) {
            directorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterDirectors(newValue);
                updateStatusLabel();
            });
        }
    }
    
    /**
     * Filters the actors list based on the search query.
     * 
     * @param query The search query
     */
    private void filterActors(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredActors.setPredicate(actor -> true);
            return;
        }
        
        String lowerCaseQuery = query.trim().toLowerCase();
        filteredActors.setPredicate(actor -> {
            if (actor == null) return false;
            
            // Check if name matches (first name, last name, or full name)
            String fullName = (actor.getFirstName() + " " + actor.getLastName()).toLowerCase();
            String firstName = actor.getFirstName() != null ? actor.getFirstName().toLowerCase() : "";
            String lastName = actor.getLastName() != null ? actor.getLastName().toLowerCase() : "";

            if (fullName.contains(lowerCaseQuery) || 
                firstName.contains(lowerCaseQuery) || 
                lastName.contains(lowerCaseQuery)) {
                return true;
            }
            
            // Check if any notable work contains the query
            if (actor.getNotableWorks() != null) {
                return actor.getNotableWorks().stream()
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .anyMatch(work -> work.contains(lowerCaseQuery));
            }
            
            return false;
        });
        
        // Log the number of filtered results
        logger.debug("Filtered {} actors for query: {}", filteredActors.size(), query);
    }
    
    /**
     * Filters the directors list based on the search query.
     * 
     * @param query The search query
     */
    private void filterDirectors(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredDirectors.setPredicate(director -> true);
            return;
        }
        
        String lowerCaseQuery = query.trim().toLowerCase();
        filteredDirectors.setPredicate(director -> {
            if (director == null) return false;
            
            // Check if name matches (first name, last name, or full name)
            String fullName = (director.getFirstName() + " " + director.getLastName()).toLowerCase();
            String firstName = director.getFirstName() != null ? director.getFirstName().toLowerCase() : "";
            String lastName = director.getLastName() != null ? director.getLastName().toLowerCase() : "";
            
            if (fullName.contains(lowerCaseQuery) || 
                firstName.contains(lowerCaseQuery) || 
                lastName.contains(lowerCaseQuery)) {
                return true;
            }
            
            // Check if any notable work contains the query
            if (director.getNotableWorks() != null) {
                return director.getNotableWorks().stream()
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .anyMatch(work -> work.contains(lowerCaseQuery));
            }
            
            return false;
        });
        
        // Log the number of filtered results
        logger.debug("Filtered {} directors for query: {}", filteredDirectors.size(), query);
    }
    
    /**
     * Updates the status label with the current number of filtered results.
     */
    private void updateStatusLabel() {
        if (statusLabel != null) {
            long actorCount = filteredActors.size();
            long directorCount = filteredDirectors.size();
            
            String message = String.format("Found %d actor%s and %d director%s",
                actorCount, actorCount != 1 ? "s" : "",
                directorCount, directorCount != 1 ? "s" : "");
                
            statusLabel.setText(message);
        }
    }
    

    /**
     * Loads celebrities (both actors and directors) from the services and updates the UI.
     * This method is designed to be run in a background thread.
     */
    private void loadCelebrities() {
        // Initialize empty lists to avoid NPEs in case of errors
        List<Actor> actorList = Collections.emptyList();
        List<Director> directorList = Collections.emptyList();
        
        try {
            // Load actors with notable works
            if (actorService != null) {
                actorList = actorService.getAll();
                logger.info("Loaded {} actors", actorList.size());
                
                // Log notable works for first few actors
                int actorsToLog = Math.min(5, actorList.size());
                for (int i = 0; i < actorsToLog; i++) {
                    Actor actor = actorList.get(i);
                    logger.debug("Actor[{}]: {} {}", i, actor.getFirstName(), actor.getLastName());
                    logger.debug("  Notable Works: {}", 
                        actor.getNotableWorks() != null && !actor.getNotableWorks().isEmpty() ? 
                        String.join(", ", actor.getNotableWorks()) : "None");
                }
            } else {
                logger.warn("Actor service is not initialized");
            }

            // Load directors with notable works
            if (directorService != null) {
                directorList = directorService.getAll();
                logger.info("Loaded {} directors", directorList.size());
                
                // Log notable works for first few directors
                int directorsToLog = Math.min(5, directorList.size());
                for (int i = 0; i < directorsToLog; i++) {
                    Director director = directorList.get(i);
                    logger.debug("Director[{}]: {} {}", i, director.getFirstName(), director.getLastName());
                    logger.debug("  Notable Works: {}", 
                        director.getNotableWorks() != null && !director.getNotableWorks().isEmpty() ? 
                        String.join(", ", director.getNotableWorks()) : "None");
                }
            } else {
                logger.warn("Director service is not initialized");
            }
            
            // Update UI on the JavaFX Application Thread
            updateUIWithCelebrities(actorList, directorList);
            
        } catch (Exception e) {
            String errorMsg = "Failed to load celebrities: " + e.getMessage();
            logger.error(errorMsg, e);
            
            // Update UI with empty lists in case of error
            updateUIWithCelebrities(Collections.emptyList(), Collections.emptyList());
            
            // Show error to user
            Platform.runLater(() -> 
                showError("Load Error", "Failed to load celebrities. Please try again later.")
            );
        }
    }
    
    /**
     * Updates the status label with the given message.
     * 
     * @param message The message to display in the status label
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> {
                statusLabel.setText(message);
                
                // Clear the status after 5 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        if (statusLabel.getText().equals(message)) {
                            Platform.runLater(() -> statusLabel.setText(""));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
    }
}
