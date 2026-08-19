package com.papel.imdb_clone.controllers.people;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Director;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class CelebritiesDialogHelper {

    private static final Logger logger = LoggerFactory.getLogger(CelebritiesDialogHelper.class);

    public Optional<Actor> showAddActorDialog(Actor actorToEdit) {
        try {
            Dialog<Actor> dialog = new Dialog<>();
            dialog.setTitle(actorToEdit != null ? "Edit Actor" : "Add New Actor");
            dialog.setHeaderText("Enter actor details");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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

            Platform.runLater(firstNameField::requestFocus);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String firstName = firstNameField.getText().trim();
                        String lastName = lastNameField.getText().trim();
                        LocalDate birthDate = birthDatePicker.getValue();
                        char gender = genderComboBox.getValue() != null ? genderComboBox.getValue().charAt(0) : 'U';
                        Ethnicity ethnicity = ethnicityComboBox.getValue() != null ? ethnicityComboBox.getValue() : Ethnicity.UNKNOWN;

                        String notableWorks = Arrays.stream(notableWorksArea.getText().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.joining(", "));

                        Actor actor = new Actor(firstName, lastName, birthDate, gender, ethnicity);
                        actor.setNotableWorks(notableWorks);

                        return actor;
                    } catch (Exception e) {
                        logger.error("Error creating actor", e);
                        return null;
                    }
                }
                return null;
            });

            Optional<Actor> result = dialog.showAndWait();
            return result;
        } catch (Exception e) {
            logger.error("Error in showAddActorDialog", e);
            return Optional.empty();
        }
    }

    public Optional<Director> showAddDirectorDialog(Director directorToEdit) {
        try {
            Dialog<Director> dialog = new Dialog<>();
            dialog.setTitle(directorToEdit != null ? "Edit Director" : "Add New Director");
            dialog.setHeaderText("Enter director details");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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

            Platform.runLater(firstNameField::requestFocus);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String firstName = firstNameField.getText().trim();
                        String lastName = lastNameField.getText().trim();
                        LocalDate birthDate = birthDatePicker.getValue();
                        char gender = genderComboBox.getValue() != null ? genderComboBox.getValue().charAt(0) : 'U';
                        Ethnicity ethnicity = ethnicityComboBox.getValue() != null ? ethnicityComboBox.getValue() : Ethnicity.UNKNOWN;

                        String notableWorks = Arrays.stream(notableWorksArea.getText().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.joining(", "));

                        Director director = Director.getInstance(firstName, lastName, birthDate, gender);
                        director.setNotableWorks(notableWorks);

                        return director;
                    } catch (Exception e) {
                        logger.error("Error creating director", e);
                        return null;
                    }
                }
                return null;
            });

            @SuppressWarnings("unchecked")
            Optional<Director> result = (Optional<Director>) (Optional<?>) dialog.showAndWait();
            return result;
        } catch (Exception e) {
            logger.error("Error in showAddDirectorDialog", e);
            return Optional.empty();
        }
    }

    public Optional<Actor> showAddCelebrityDialogAsActor() {
        return showAddActorDialog(null);
    }
}