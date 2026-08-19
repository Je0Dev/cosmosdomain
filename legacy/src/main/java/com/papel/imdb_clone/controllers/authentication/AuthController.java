package com.papel.imdb_clone.controllers.authentication;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.controllers.MainController;
import com.papel.imdb_clone.model.people.User;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.papel.imdb_clone.service.validation.AuthService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.validation.UserInputValidator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;


/**
 * Controller for handling user authentication including login and registration.
 * Manages the authentication UI and coordinates with AuthService for user operations.
 */

public class AuthController extends BaseController implements Initializable {

    // Enhanced logging configuration
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    //transient means that the variable is not serialized which means it is not saved to the database
    private final transient UserInputValidator inputValidator; //user input validator for validationz
    private final transient AuthService authService; //authentication service for authentication
    private final transient NavigationService navigationService; //navigation service for navigation
    private transient String sessionToken; //session token for authentication
    private final transient Map<String, Serializable> data; //data to be passed to the next controller
    private final com.papel.imdb_clone.service.validation.AuthValidationHelper validationHelper = new com.papel.imdb_clone.service.validation.AuthValidationHelper();
    private Stage stage;


    //Login
    @FXML
    private Button loginButton;
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginErrorLabel;
    @FXML private TextField passwordVisibleField;
    @FXML private TextField loginPasswordVisibleField;

    //Registration
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField registerUsernameField;
    @FXML private Button registerButton;
    @FXML private StackPane loginContainer;
    @FXML private StackPane registerContainer;
    @FXML public TextField registerPasswordVisibleField;

    @FXML private TextField confirmPasswordVisibleField;



    /**
     * Constructs a new AuthController with the specified dependencies.
     */
    public AuthController() {
        //call the constructor of the parent class which is BaseController
        super();
        this.authService = AuthService.getInstance();
        this.navigationService = NavigationService.getInstance();
        this.inputValidator = new UserInputValidator();
        this.sessionToken = null;
        this.data = null;

        // Add shutdown hook to clean up session token when application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.sessionToken != null) {
                this.sessionToken = null;
                System.out.println("Session token cleared.");
                authService.logout(null);
            }
        }));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialize all UI components first
            if (loginContainer != null) {
                logger.info("Login container found, initializing login view...");
                initializeLoginView();
            } else if (registerContainer != null) {
                logger.info("Register container found, initializing register view...");
                initializeRegisterView();
            } else {
                // If no container is found, try to initialize based on available components
                logger.info("No container found, initializing based on available components...");

                boolean hasLoginComponents = loginUsernameField != null && loginPasswordField != null && loginButton != null;
                boolean hasRegisterComponents = registerUsernameField != null && registerPasswordField != null &&
                                              confirmPasswordField != null && registerButton != null;

                if (hasLoginComponents) {
                    logger.info("Login components found, initializing login form...");
                    setupLoginForm(loginPasswordVisibleField);
                } else if (hasRegisterComponents) {
                    logger.info("Register components found, initializing registration form...");
                    setupRegistrationForm(registerPasswordVisibleField, confirmPasswordVisibleField);
                } else {
                    logger.error("No valid UI components found for authentication");
                    throw new IllegalStateException("No valid authentication components found in the view");
                }
            }

            logger.debug("AuthController initialization completed successfully");
        } catch (Exception e) {
            String errorMsg = "Error initializing AuthController: " + e.getMessage();
            logger.error(errorMsg, e);
            Platform.runLater(() ->
                showError("Initialization Error", "Failed to initialize the authentication form: " + e.getMessage())
            );
        }
    }

    /**
     * Initializes the login view components
     */
    private void initializeLoginView() {
        try {
            if (loginButton != null && usernameField != null && passwordField != null) {
                // Unbind first to prevent memory leaks which means that it will not be able to be used again
                loginButton.disableProperty().unbind();

                // Set up login button binding
                loginButton.disableProperty().bind(
                    usernameField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
                );

                // Set up password visibility toggle if available
                if (passwordVisibleField != null) {
                    passwordVisibleField.visibleProperty().bind(passwordField.visibleProperty().not());
                    passwordField.visibleProperty().bind(passwordVisibleField.visibleProperty().not());
                    passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
                }

                // Handle Enter key press on password field
                passwordField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        handleLogin(new ActionEvent(loginButton, null));
                        event.consume();
                    }
                });

                logger.debug("Login view initialization complete");
            } else {
                logger.warn("Missing required login view components");
                if (loginButton == null) logger.warn("loginButton is null");
                if (usernameField == null) logger.warn("usernameField is null");
                if (passwordField == null) logger.warn("passwordField is null");
            }
        } catch (Exception e) {
            logger.error("Error initializing login view: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Initializes the register view components
     */
    private void initializeRegisterView() {
        try {
            // Set up registration form if components are available
            if (registerButton != null && registerUsernameField != null &&
                emailField != null && registerPasswordField != null && confirmPasswordField != null) {

                // Unbind first to prevent memory leaks
                registerButton.disableProperty().unbind();

                // Initialize password fields visibility
                if (registerPasswordVisibleField != null) {
                    registerPasswordVisibleField.setVisible(false);
                    registerPasswordVisibleField.textProperty().bindBidirectional(registerPasswordField.textProperty());
                }
                
                if (confirmPasswordVisibleField != null) {
                    confirmPasswordVisibleField.setVisible(false);
                    confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
                }

                // Set up form validation
                registerButton.disableProperty().bind(
                    (firstNameField != null ? firstNameField.textProperty().isEmpty() : Bindings.createBooleanBinding(() -> false))
                        .or(lastNameField != null ? lastNameField.textProperty().isEmpty() : Bindings.createBooleanBinding(() -> false))
                        .or(registerUsernameField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(() ->
                                            inputValidator.isValidUsername(registerUsernameField.getText()),
                                registerUsernameField.textProperty())))
                        .or(emailField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(() ->
                                            inputValidator.isValidEmail(emailField.getText()),
                                emailField.textProperty())))
                        .or(registerPasswordField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(
                                () -> !registerPasswordField.getText().equals(confirmPasswordField.getText()),
                                registerPasswordField.textProperty(),
                                confirmPasswordField.textProperty())))
                );

                logger.debug("Register view initialization complete");
            } else {
                logger.warn("Missing required register view components");
                if (registerButton == null) logger.warn("registerButton is null");
                if (registerUsernameField == null) logger.warn("registerUsernameField is null");
                if (emailField == null) logger.warn("emailField is null");
                if (passwordField == null) logger.warn("passwordField is null");
                if (confirmPasswordField == null) logger.warn("confirmPasswordField is null");
            }
        } catch (Exception e) {
            logger.error("Error initializing register view: {}", e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Sets the primary stage for this controller.
     *
     * @param stage The primary stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }



    @FXML
    private TextField emailField;
    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    
    // Tracks password visibility state
    private boolean isLoginPasswordVisible = false;
    private boolean isRegisterPasswordVisible = false;
    
    /**
     * Toggles the visibility of the login password field.
     * This method is called when the user clicks the show/hide button in the login form.
     */
    @FXML
    private void toggleLoginPasswordVisibility() {
        if (passwordField == null) return;
        
        isLoginPasswordVisible = !isLoginPasswordVisible;
        
        if (isLoginPasswordVisible) {
            // Change to text field
            passwordField.getScene().lookup("#passwordField").setVisible(false);
            TextField visibleField = (TextField) passwordField.getScene().lookup("#loginPasswordVisibleField");
            if (visibleField == null) {
                visibleField = new TextField();
                visibleField.setId("loginPasswordVisibleField");
                visibleField.setStyle(
                    "-fx-pref-width: 300; " +
                    "-fx-background-color: #2a2a2a; " +
                    "-fx-text-fill: #ffffff; " +
                    "-fx-prompt-text-fill: #666666; " +
                    "-fx-border-color: #333333; " +
                    "-fx-border-radius: 5; " +
                    "-fx-padding: 10;"
                );
                ((VBox) passwordField.getParent()).getChildren().add(1, visibleField);
            }
            visibleField.setText(passwordField.getText());
            visibleField.setVisible(true);
            
            // Update button text to hide
            for (Node node : passwordField.getParent().getChildrenUnmodifiable()) {
                if (node instanceof Button) {
                    ((Button) node).setText("Hide");
                }
            }
        } else {
            // Change back to password field
            TextField visibleField = (TextField) passwordField.getScene().lookup("#loginPasswordVisibleField");
            if (visibleField != null) {
                passwordField.setText(visibleField.getText());
                visibleField.setVisible(false);
            }
            passwordField.setVisible(true);
            
            // Update button text
            for (Node node : passwordField.getParent().getChildrenUnmodifiable()) {
                if (node instanceof Button) {
                    ((Button) node).setText("Show");
                }
            }
        }
    }
    
    /**
     * Toggles the visibility of the registration password fields.
     * This method is called when the user clicks the show/hide button in the registration form.
     */
    @FXML
    private void toggleRegisterPasswordVisibility() {
        isRegisterPasswordVisible = !isRegisterPasswordVisible;
        
        if (isRegisterPasswordVisible) {
            // Show the passwords
            registerPasswordVisibleField.setText(registerPasswordField.getText());
            confirmPasswordVisibleField.setText(confirmPasswordField.getText());
            
            registerPasswordVisibleField.setVisible(true);
            registerPasswordField.setVisible(false);
            confirmPasswordVisibleField.setVisible(true);
            confirmPasswordField.setVisible(false);
            
            // Find the show/hide button and update its text
            for (Node node : registerPasswordField.getParent().getChildrenUnmodifiable()) {
                if (node instanceof Button) {
                    ((Button) node).setText("Hide");
                }
            }
        } else {
            // Hide the passwords
            registerPasswordField.setText(registerPasswordVisibleField.getText());
            confirmPasswordField.setText(confirmPasswordVisibleField.getText());
            
            registerPasswordField.setVisible(true);
            registerPasswordVisibleField.setVisible(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordVisibleField.setVisible(false);
            
            // Find the show/hide button and update its text
            for (Node node : registerPasswordField.getParent().getChildrenUnmodifiable()) {
                if (node instanceof Button) {
                    ((Button) node).setText("Show");
                }
            }
        }
    }

    /**
     * Navigates to the login view
     */
    @FXML
    public void navigateToLogin() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login-view.fxml"));
            Parent root = loader.load();

            // Get the current stage from any node in the current scene
            Stage stage = (Stage) errorLabel.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error navigating to login view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load login view. Please try again.");
        }
    }


    /**
     * Initializes the controller with the current user ID.
     * @param currentUserId The ID of the currently logged-in user, or -1 if no user is logged in
     */
    @Override
    protected void initializeController(int currentUserId) {
        try {
            //setup login and registration forms
            setupLoginForm(loginPasswordVisibleField);
            setupRegistrationForm(passwordVisibleField, confirmPasswordVisibleField);
        } catch (Exception e) {
            logger.error("Error initializing AuthController: {}", e.getMessage(), e);
            UIUtils.showError("Initialization Error", "Failed to initialize authentication system.");
        }
    }

    /**
     * Sets up the login form with necessary bindings and listeners.
     *
     * @param loginPasswordVisibleField The text field for showing/hiding the password
     */
    private void setupLoginForm(TextField loginPasswordVisibleField) {
        try {
            logger.debug("Setting up login form");

            if (loginPasswordVisibleField == null) {
                logger.warn("loginPasswordVisibleField is null");
                return;
            }

            this.loginPasswordVisibleField = loginPasswordVisibleField;

            // Only set up bindings if we have all required components
            if (loginButton != null && loginUsernameField != null && loginPasswordField != null) {
                // Unbind any existing bindings to prevent memory leaks
                loginButton.disableProperty().unbind();

                // Set up new bindings
                loginButton.disableProperty().bind(
                    loginUsernameField.textProperty().isEmpty()
                        .or(loginPasswordField.textProperty().isEmpty())
                );

                // Set up password visibility toggle if we have the visible field
                loginPasswordVisibleField.visibleProperty().bind(loginPasswordField.visibleProperty().not());
                loginPasswordField.visibleProperty().bind(loginPasswordVisibleField.visibleProperty().not());
                loginPasswordVisibleField.textProperty().bindBidirectional(loginPasswordField.textProperty());

                logger.debug("Login form setup complete");
            } else {
                logger.warn("Cannot set up login form - missing required components");
                if (loginButton == null) logger.warn("loginButton is null");
                if (loginUsernameField == null) logger.warn("loginUsernameField is null");
                if (loginPasswordField == null) logger.warn("loginPasswordField is null");
            }
        } catch (Exception e) {
            logger.error("Error in setupLoginForm: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sets up the registration form with validation and bindings.
     *
     * @param passwordVisibleField        The text field for showing/hiding the password
     * @param confirmPasswordVisibleField The text field for showing/hiding the confirm password
     */
    private void setupRegistrationForm(TextField passwordVisibleField, TextField confirmPasswordVisibleField) {
        try {
            logger.debug("Setting up registration form");

            if (passwordVisibleField == null || confirmPasswordVisibleField == null) {
                logger.warn("Password visibility fields are not properly initialized");
                return;
            }

            this.passwordVisibleField = passwordVisibleField;
            this.confirmPasswordVisibleField = confirmPasswordVisibleField;

            // Check if all required fields are available
            if (registerButton == null || firstNameField == null || lastNameField == null ||
                    registerUsernameField == null || emailField == null ||
                    passwordField == null || confirmPasswordField == null) {

                logger.warn("Cannot set up registration form - missing required components");
                if (registerButton == null) logger.warn("registerButton is null");
                if (firstNameField == null) logger.warn("firstNameField is null");
                if (lastNameField == null) logger.warn("lastNameField is null");
                if (registerUsernameField == null) logger.warn("registerUsernameField is null");
                if (emailField == null) logger.warn("emailField is null");
                if (passwordField == null) logger.warn("passwordField is null");
                if (confirmPasswordField == null) logger.warn("confirmPasswordField is null");
                return;
            }

            // Unbind any existing bindings to prevent memory leaks
            registerButton.disableProperty().unbind();

            // Set up password visibility toggle
            passwordVisibleField.visibleProperty().bind(passwordField.visibleProperty().not());
            passwordField.visibleProperty().bind(passwordVisibleField.visibleProperty().not());
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

            confirmPasswordVisibleField.visibleProperty().bind(confirmPasswordField.visibleProperty().not());
            confirmPasswordField.visibleProperty().bind(confirmPasswordVisibleField.visibleProperty().not());
            confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

            // Set up form validation
            registerButton.disableProperty().bind(
                    firstNameField.textProperty().isEmpty()
                            .or(lastNameField.textProperty().isEmpty())
                            .or(registerUsernameField.textProperty().isEmpty()
                                    .or(Bindings.createBooleanBinding(() ->
                                                    inputValidator.isValidUsername(registerUsernameField.getText()),
                                            registerUsernameField.textProperty())))
                            .or(emailField.textProperty().isEmpty()
                                    .or(Bindings.createBooleanBinding(() ->
                                                    inputValidator.isValidEmail(emailField.getText()),
                                            emailField.textProperty())))
                            .or(registerPasswordField.textProperty().isEmpty()
                                    .or(Bindings.createBooleanBinding(
                                            () -> !registerPasswordField.getText().equals(confirmPasswordField.getText()),
                                            registerPasswordField.textProperty(),
                                            confirmPasswordField.textProperty())))
            );

            // Handle Enter key press in password fields
            registerPasswordField.setOnKeyPressed(this::handleRegisterKeyPress);
            confirmPasswordField.setOnKeyPressed(this::handleRegisterKeyPress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Handles key press events in the registration form.
     * Submits the form when Enter key is pressed.
     *
     * @param keyEvent The key event that was triggered
     */
    private void handleRegisterKeyPress(KeyEvent keyEvent) {
        try {
            if (keyEvent != null && keyEvent.getCode() == KeyCode.ENTER) {
                logger.debug("Enter key pressed in registration form");
                // Fire the register button action event which means that the register button will be clicked
                registerButton.fire();
            }
        } catch (Exception e) {
            logger.error("Error handling key press in registration form: {}", e.getMessage(), e);
            showError("UI Error", "Failed to handle key press in registration form: " + e.getMessage());
        }
    }


    /**
     * Handles the registration process.
     */
    @FXML
    public void navigateToRegister(ActionEvent event) {
        try {
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Load the register view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/register-view.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set it up
            AuthController controller = loader.getController();
            controller.setStage(stage);
            
            // Create and show the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
            logger.info("Successfully navigated to register view");
        } catch (Exception e) {
            logger.error("Error navigating to register: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load registration view. Please try again.");
        }
    }

    /**
     * Handles the registration process.
     * @param event The action event that triggered the registration process
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            // Debug logging for field states
            logger.debug("Register fields - Username: {}, Email: {}", 
                registerUsernameField != null ? "[not null]" : "[null]",
                emailField != null ? "[not null]" : "[null]");
            logger.debug("Password fields - register: {}, confirm: {}, visible: {}", 
                registerPasswordField != null ? "[not null]" : "[null]",
                confirmPasswordField != null ? "[not null]" : "[null]",
                registerPasswordVisibleField != null ? "[not null]" : "[null]");

            if (registerUsernameField == null || emailField == null || registerPasswordField == null || confirmPasswordField == null) {
                logger.error("UI components not properly initialized when trying to create account on register view");
                logger.error("Fields - username: {}, email: {}, pass: {}, confirm: {}",
                    registerUsernameField != null, emailField != null,
                    registerPasswordField != null, confirmPasswordField != null);
                return;
            }

            String username = registerUsernameField.getText().trim();
            String email = emailField.getText().trim();
            
            // Get password from visible field if it's visible, otherwise from password field
            String password = isRegisterPasswordVisible ? 
                (registerPasswordVisibleField != null ? registerPasswordVisibleField.getText() : "") :
                registerPasswordField.getText();
                
            String confirmPassword = isRegisterPasswordVisible ?
                (confirmPasswordVisibleField != null ? confirmPasswordVisibleField.getText() : "") :
                confirmPasswordField.getText();
                
            logger.debug("Password values - pass: [{}], confirm: [{}], isVisible: {}", 
                password.isEmpty() ? "empty" : "not empty", 
                confirmPassword.isEmpty() ? "empty" : "not empty",
                isRegisterPasswordVisible);

            // Basic validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showError("Validation Error", "All fields are required");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Validation Error", "Passwords do not match");
                return;
            }

            // Create user object with password
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);  // Set the password in the User object
            // Set other required fields as needed
            newUser.setFirstName(firstNameField != null ? firstNameField.getText().trim() : "");
            newUser.setLastName(lastNameField != null ? lastNameField.getText().trim() : "");

            // Register user
            logger.debug("Attempting to register user: {}", username);
            User registeredUser = authService.register(newUser, password, confirmPassword);

            if (registeredUser != null) {
                navigateToMainView(registeredUser);
            }
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            showError("Registration Error", "An error occurred during registration. Please try again.");
        }
    }

    /**
     * Navigates to the login view.
     * @param event The action event that triggered the navigation
     */
    @FXML
    public void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login-view.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (stage == null) {
                stage = new Stage();
                stage.setTitle("Login");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error navigating to login: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load login view. Please try again.");
        }
    }


    //show error message
    public void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Handle login action
     * @param event The action event that triggered the login action
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            if (usernameField == null || passwordField == null) {
                logger.error("Login form not properly initialized");
                showError("Login Error", "Login form is not properly initialized. Please try again.");
                return;
            }

            String usernameOrEmail = usernameField.getText().trim();
            String password = passwordField.getText();

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                if (loginErrorLabel != null) {
                    loginErrorLabel.setText("Please enter both username/email and password");
                    loginErrorLabel.setVisible(true);
                } else {
                    showError("Login Error", "Please enter both username/email and password");
                }
                return;
            }

            // Clear any previous errors
            if (loginErrorLabel != null) {
                loginErrorLabel.setText("");
                loginErrorLabel.setVisible(false);
            }

            // Login the user
            String sessionToken = authService.login(usernameOrEmail, password);
            if (sessionToken != null) {
                // Store the session token
                this.sessionToken = sessionToken;
                logger.info("User '{}' logged in successfully. Session token stored.", usernameOrEmail);
                
                // Get the user from the session token
                User user = authService.getUserFromSession(sessionToken);
                if (user != null) {
                    navigateToMainView(user);
                } else {
                    String errorMsg = "Failed to retrieve user information";
                    logger.error(errorMsg);
                    if (loginErrorLabel != null) {
                        loginErrorLabel.setText(errorMsg);
                        loginErrorLabel.setVisible(true);
                    } else {
                        showError("Login Failed", errorMsg);
                    }
                }
            } else {
                String errorMsg = "Invalid username/email or password";
                if (loginErrorLabel != null) {
                    loginErrorLabel.setText(errorMsg);
                    loginErrorLabel.setVisible(true);
                } else {
                    showError("Login Failed", errorMsg);
                }
            }
        } catch (Exception e) {
            String errorMsg = "An error occurred during login. Please try again.";
            logger.error("Login error: {}", e.getMessage(), e);
            if (loginErrorLabel != null) {
                loginErrorLabel.setText(errorMsg);
                loginErrorLabel.setVisible(true);
            } else {
                showError("Login Error", errorMsg);
            }
        }
    }

    //navigate to main view
    private void navigateToMainView(User user) {
        try {
            // Get the current session token
            String sessionToken = authService.getCurrentSessionToken();
            logger.info("Navigating to main view with user: {} and session token: {}",
                user != null ? user.getUsername() : "null",
                sessionToken != null ? "[HIDDEN]" : "null");

            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/base/home-view.fxml"));
            Parent root = loader.load();

            // Get the MainController and set the user and session token
            MainController mainController = loader.getController();

            // Set the session token and user in a thread-safe way
            Platform.runLater(() -> {
                try {
                    // Set the session token first
                    if (sessionToken != null) {
                        mainController.setSessionToken(sessionToken);
                        logger.debug("Session token set in MainController");
                    }

                    // Set the user and update UI states
                    mainController.setUser(user);
                    mainController.setGuest(false);

                    // Update the UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        mainController.updateUIForLoggedInUser(user);
                        mainController.updateUIForAuthState(true);
                        mainController.updateUserInterface();
                        logger.debug("UI updated for logged-in user: {}", user != null ? user.getUsername() : "null");
                    });

                } catch (Exception e) {
                    logger.error("Error updating UI after login: {}", e.getMessage(), e);
                    showError("Navigation Error", "Failed to update UI after login: " + e.getMessage());
                }
            });

            // Get the current stage
            Stage stage = (Stage) (loginButton != null ? loginButton.getScene().getWindow() :
                    registerButton != null ? registerButton.getScene().getWindow() :
                    usernameField != null ? usernameField.getScene().getWindow() : null);

            if (stage != null) {
                // Set the new scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("IMDb Clone - Home");
                stage.show();
                logger.info("Successfully navigated to main view");

                // Force a refresh of the UI
                Platform.runLater(() -> {
                    mainController.updateUIForAuthState(true);
                });
            } else {
                throw new IllegalStateException("Could not determine current stage");
            }
        } catch (Exception e) {
            logger.error("Error navigating to main view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load main application view. Please try again.");
        }
    }

    /**
     * Continues as a guest user
     * @param actionEvent The action event that triggered the guest continuation
     */
    @FXML
    public void continueAsGuest(ActionEvent actionEvent) {
        try {
            // Load the main view as guest (user ID -1)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/base/home-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setStage(stage);
            mainController.updateUIForAuthState(false);

            // Get the current stage
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error continuing as guest: {}", e.getMessage(), e);
            showError("Guest Access Error", "Failed to load application as guest. Please try again.");
        }
    }
}
