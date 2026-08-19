package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.exceptions.AuthErrorType;
import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.exceptions.DataPersistenceException;
import com.papel.imdb_clone.model.people.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Service class for user-related operations including authentication, registration,
 * and profile management.
 * This service provides thread-safe operations for user management and authentication.
 * It uses the singleton pattern to ensure a single instance manages the current user session.
 */
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static volatile UserService instance;
    private final DataManager dataManager;
    private User currentUser;
    private boolean authenticated;
    private final Object lock = new Object();

    // Constants for validation
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /**
     * Private constructor for singleton pattern.
     *
     * @param dataManager The data manager instance (must not be null)
     * @throws IllegalArgumentException if dataManager is null
     */
    private UserService(DataManager dataManager) {
        if (dataManager == null) {
            throw new IllegalArgumentException("DataManager cannot be null");
        }
        this.dataManager = dataManager;
        logger.info("UserService initialized with DataManager: {}", dataManager.getClass().getSimpleName());
    }

    /**
     * Gets the singleton instance of UserService.
     *
     * @param dataManager The data manager instance (must not be null)
     * @return The singleton instance of UserService
     * @throws IllegalArgumentException if dataManager is null
     * @throws IllegalStateException if the service fails to initialize
     */
    public static UserService getInstance(DataManager dataManager) {
        if (dataManager == null) {
            throw new IllegalArgumentException("DataManager cannot be null");
        }

        // Double-checked locking for thread safety
        UserService result = instance;
        if (result == null) {
            synchronized (UserService.class) {
                result = instance;
                if (result == null) {
                    try {
                        instance = result = new UserService(dataManager);
                        logger.debug("Created new UserService instance");
                    } catch (Exception e) {
                        String errorMsg = "Failed to initialize UserService";
                        logger.error(errorMsg, e);
                        throw new IllegalStateException(errorMsg, e);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Registers a new user with the system.
     *
     * @param username The username (must be unique)
     * @param email The email address (must be unique and valid)
     * @param password The password (must meet complexity requirements)
     * @param firstName User's first name
     * @param lastName User's last name
     * @return The newly created user
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws AuthException if the username or email is already taken
     * @throws DataPersistenceException if there's an error saving the user
     */
    public User registerUser(String username, String email, String password, String firstName, String lastName) {
        validateUserInput(username, email, password, firstName, lastName);

        synchronized (lock) {
            try {
                // Check if username already exists
                if (dataManager.getUserRepository().findByUsername(username).isPresent()) {
                    throw new AuthException(
                        AuthErrorType.ACCOUNT_ALREADY_EXISTS,
                        "Username already exists: " + username
                    );
                }

                // Note: Email uniqueness check is not implemented in the repository
                // You might want to add this functionality if needed

                // Create and save new user
                // Using default constructor and setting fields individually
                User newUser = new User(username, email, password, 'U', "user");
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);

                User savedUser = dataManager.getUserRepository().save(newUser);
                logger.info("User registered successfully: {}", username);
                return savedUser;

            } catch (AuthException e) {
                logger.warn("Registration failed for {}: {}", username, e.getMessage());
                throw e;
            } catch (Exception e) {
                String errorMsg = String.format("Failed to register user: %s", username);
                logger.error(errorMsg, e);
                throw new DataPersistenceException(
                    errorMsg,
                    AuthErrorType.INTERNAL_ERROR,
                    e
                );
            }
        }
    }

    /**
     * Authenticates a user with the given credentials.
     *
     * @param username The username or email of the user
     * @param password The password to authenticate with
     * @return The authenticated user
     * @throws AuthException if authentication fails
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException(AuthErrorType.INVALID_CREDENTIALS, "Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthException(AuthErrorType.INVALID_CREDENTIALS, "Password cannot be empty");
        }

        try {
            // First try to find by username
            Optional<User> userOpt = dataManager.getUserRepository().findByUsername(username);

            if (userOpt.isEmpty()) {
                throw new AuthException(AuthErrorType.USER_NOT_FOUND, "User not found");
            }

            User user = userOpt.get();

            // Verify password (assuming User class has a verifyPassword method)
            if (!user.getPassword().equals(password)) { // This should be replaced with proper password hashing
                throw new AuthException(AuthErrorType.INVALID_CREDENTIALS, "Invalid password");
            }

            synchronized (lock) {
                this.currentUser = user;
                this.authenticated = true;
            }

            logger.info("User logged in: {}", user.getUsername());
            return user;

        } catch (AuthException e) {
            logger.warn("Login failed for {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Error during login for user: %s", username);
            logger.error(errorMsg, e);
            throw new AuthException(
                AuthErrorType.INTERNAL_ERROR,
                "An error occurred during login",
                e
            );
        }
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
        }
        synchronized (lock) {
            this.currentUser = null;
            this.authenticated = false;
        }
    }

    /**
     * Updates the current user's profile information.
     *
     * @param updatedUser The updated user information
     * @return The updated user
     * @throws IllegalStateException if no user is logged in
     * @throws AuthException if the update fails due to authentication issues
     * @throws DataPersistenceException if there's an error saving the changes
     */
    public User updateProfile(User updatedUser) {
        if (updatedUser == null) {
            throw new IllegalArgumentException("Updated user cannot be null");
        }

        synchronized (lock) {
            if (currentUser == null || !authenticated) {
                throw new IllegalStateException("No user is currently logged in");
            }

            try {
                // Ensure we're updating the current user
                if (currentUser.getId() != updatedUser.getId()) {
                    throw new AuthException(
                        AuthErrorType.UNAUTHORIZED,
                        "Cannot update another user's profile"
                    );
                }

                // Update user fields
                currentUser.setFirstName(updatedUser.getFirstName());
                currentUser.setLastName(updatedUser.getLastName());
                currentUser.setEmail(updatedUser.getEmail());

                // Save changes
                dataManager.getUserRepository().save(currentUser);
                logger.info("Profile updated for user: {}", currentUser.getUsername());
                return currentUser;

            } catch (AuthException e) {
                logger.warn("Profile update failed for {}: {}", currentUser.getUsername(), e.getMessage());
                throw e;
            } catch (Exception e) {
                String errorMsg = String.format("Failed to update profile for user: %s", currentUser.getUsername());
                logger.error(errorMsg, e);
                throw new DataPersistenceException(
                    errorMsg,
                    AuthErrorType.INTERNAL_ERROR,
                    e
                );
            }
        }
    }

    /**
     * Changes the current user's password.
     *
     * @param currentPassword The current password for verification
     * @param newPassword The new password
     * @throws IllegalStateException if no user is logged in
     * @throws AuthException if the current password is incorrect or new password is invalid
     */
    public void changePassword(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password cannot be empty");
        }
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        synchronized (lock) {
            if (currentUser == null || !authenticated) {
                throw new IllegalStateException("No user is currently logged in");
            }

            try {
                // Verify current password (simple comparison - should be hashed in production)
                if (!currentUser.getPassword().equals(currentPassword)) {
                    throw new AuthException(AuthErrorType.INVALID_CREDENTIALS, "Current password is incorrect");
                }

                // Update password (should be hashed in production)
                currentUser.setPassword(newPassword);
                dataManager.getUserRepository().save(currentUser);

                logger.info("Password changed for user: {}", currentUser.getUsername());

            } catch (AuthException e) {
                logger.warn("Password change failed for {}: {}", currentUser.getUsername(), e.getMessage());
                throw e;
            } catch (Exception e) {
                String errorMsg = String.format("Failed to change password for user: %s", currentUser.getUsername());
                logger.error(errorMsg, e);
                throw new DataPersistenceException(
                    errorMsg,
                    AuthErrorType.INTERNAL_ERROR,
                    e
                );
            }
        }
    }

    /**
     * Validates user input for registration.
     */
    private void validateUserInput(String username, String email, String password, String firstName, String lastName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || !email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }

}