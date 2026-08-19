package com.papel.imdb_clone.service.validation;

import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.exceptions.InvalidInputException;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.people.UserStorageService;
import com.papel.imdb_clone.repository.UserRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * Service responsible for user authentication and session management.
 * Handles user registration, login, logout, and session validation.
 * Uses unified AuthException for all authentication-related errors.
 */
public class AuthService {
    
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // Configuration constants
    private static final String USERS_UPDATED_FILE = "src/main/resources/data/people/users_updated.txt";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_ATTEMPT_WINDOW_MINUTES = 15;
    private static final long SESSION_TIMEOUT_MINUTES = 30;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]{3,20}$");

    // Singleton instance
    private static volatile AuthService instance;

    // User storage
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private final UserStorageService userStorageService;
    private final UserRepository userRepository;
    private int nextUserId = 1;
    private String sessionToken;

    public String getCurrentSessionToken() {
        return sessionToken;
    }

    /**
     * Checks if there is an active authenticated session.
     * @return true if a user is currently authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        boolean isAuthenticated = sessionToken != null && !sessionToken.isEmpty() && 
               userSessions.containsKey(sessionToken);
        
        if (isAuthenticated) {
            String username = userSessions.get(sessionToken);
            logger.debug("Session check - User '{}' is authenticated with token: {}...", 
                username, sessionToken.substring(0, Math.min(8, sessionToken.length())));
        } else {
            logger.debug("Session check - No active session found");
        }
        
        return isAuthenticated;
    }

    public User getUserFromSession(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            logger.debug("getUserFromSession: No session token provided");
            return null;
        }
        
        String username = userSessions.get(sessionToken);
        String substring = sessionToken.substring(0, Math.min(8, sessionToken.length()));
        if (username == null) {
            logger.warn("getUserFromSession: No user found for session token: {}...",
                    substring);
            return null;
        }
        
        User user = usersByUsername.get(username.toLowerCase());
        if (user != null) {
            logger.debug("getUserFromSession: Found user '{}' for session token: {}...", 
                username, substring);
        } else {
            logger.error("getUserFromSession: User '{}' not found in users map for session token: {}...", 
                username, substring);
        }
        
        return user;
    }

    /**
     * Tracks login attempts for rate limiting.
     */
    private static class LoginAttempt {
        private final Instant timestamp;
        private int attempts;

        public LoginAttempt() {
            this.timestamp = Instant.now();
            this.attempts = 1;
        }

        public synchronized void incrementAttempts() {
            this.attempts++;
        }

        public synchronized boolean isLocked() {
            return attempts >= MAX_LOGIN_ATTEMPTS &&
                    Duration.between(timestamp, Instant.now()).toMinutes() < LOGIN_ATTEMPT_WINDOW_MINUTES;
        }

        public synchronized long getRemainingLockTime() {
            if (!isLocked()) return 0;
            long elapsedMinutes = Duration.between(timestamp, Instant.now()).toMinutes();
            return LOGIN_ATTEMPT_WINDOW_MINUTES - elapsedMinutes;
        }

        public synchronized int getAttempts() {
            return attempts;
        }
    }

    /**
     * Gets the map of usernames to User objects.
     *
     * @return An unmodifiable map of usernames to User objects
     */
    public Map<String, User> getUsersByUsername() {
        return Collections.unmodifiableMap(usersByUsername);
    }

    private AuthService() {
        this.userStorageService = UserStorageService.getInstance();
        this.userRepository = new InMemoryUserRepository();
        logger.info("Initializing AuthService...");
        try {
            loadUsers();
            startSessionCleanupTask();
        } catch (Exception e) {
            logger.error("Failed to initialize AuthService: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize AuthService", e);
        }
    }

    /**
     * Starts a background task to clean up expired sessions.
     */
    private void startSessionCleanupTask() {
        Timer timer = new Timer("SessionCleanupTimer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredSessions();
            }
        }, TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(5));
    }

    /**
     * Cleans up expired user sessions.
     */
    private synchronized void cleanupExpiredSessions() {
        try {
            int initialSize = userSessions.size();
            Instant now = Instant.now();

            userSessions.entrySet().removeIf(entry -> {
                User user = usersByUsername.get(entry.getValue());
                return user != null && user.getLastActivity() != null &&
                        Duration.between(user.getLastActivity(), now).toMinutes() > SESSION_TIMEOUT_MINUTES;
            });

            int removed = initialSize - userSessions.size();
            if (removed > 0) {
                logger.info("Cleaned up {} expired sessions", removed);
            }
        } catch (Exception e) {
            logger.error("Error during session cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads users from the configured storage sources with proper error handling.
     * Attempts to load from text file first, then falls back to serialized storage.
     * Ensures at least the default admin user exists in the system.
     */
    private void loadUsers() {
        // Clear existing users to prevent duplicates
        usersByUsername.clear();
        usersByEmail.clear();

        try {
            logger.debug("Starting user loading process");

            // First, try to load users from the text file
            loadUsersFromTextFile();
            logger.info("Loaded {} users from text file", usersByUsername.size());

            // If no users were loaded from the text file, try loading from storage
            if (usersByUsername.isEmpty()) {
                logger.debug("No users loaded from text file, trying storage");
                userStorageService.loadUsers(usersByUsername, usersByEmail);
                logger.info("Loaded {} users from storage", usersByUsername.size());
            } else {
                // Save the loaded users to the serialized storage for next time
                saveUsers();
            }

            // Ensure we have at least the default admin user
            createDefaultAdminUser();

        } catch (Exception e) {
            String errorMsg = String.format("Failed to load users: %s", e.getMessage());
            logger.error(errorMsg, e);

            // Even if loading fails, ensure we have the default admin user
            try {
                createDefaultAdminUser();
            } catch (Exception ex) {
                logger.error("Critical: Failed to create default admin user", ex);
                throw new RuntimeException("Critical: Failed to initialize authentication service", ex);
            }

            throw new RuntimeException(errorMsg, e);
        } finally {
            logger.info("User loading process completed. Total users: {}", usersByUsername.size());
        }
    }

    /**
     * Validates user credentials before authentication.
     *
     * @param username The username to validate
     * @param password The password to validate
     * @throws InvalidInputException if validation fails
     */
    private void validateCredentials(String username, String password) throws InvalidInputException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("Password cannot be empty");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new InvalidInputException("Invalid username format. Use 3-20 alphanumeric characters, '-', or '_'");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long. Please choose a stronger password.");
        }
    }

    /**
     * Validates user registration information.
     *
     * @param user The user to validate
     * @throws InvalidInputException if validation fails
     */
    private void validateUserRegistration(User user) throws InvalidInputException {
        if (user == null) {
            throw new InvalidInputException("User cannot be null");
        }

        validateCredentials(user.getUsername(), user.getPassword());

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new InvalidInputException("Invalid email format");
        }

        if (usersByUsername.containsKey(user.getUsername())) {
            throw new InvalidInputException("Username already exists");
        }

        if (usersByEmail.containsKey(user.getEmail())) {
            throw new InvalidInputException("Email already registered");
        }
    }


    /**
     * Returns the singleton instance of AuthService.
     * Uses double-checked locking for thread safety.
     *
     * @return The singleton instance of AuthService
     */
    public static AuthService getInstance() {
        AuthService result = instance;
        if (result == null) {
            synchronized (AuthService.class) {
                result = instance;
                if (result == null) {
                    instance = result = new AuthService();
                }
            }
        }
        return result;
    }

    /**
     * Logs out a user by removing their session.
     *
     * @param token The session token to invalidate
     */
    public void logout(String token) {
        if (token != null) {
            String username = userSessions.remove(token);
            if (username != null) {
                logger.info("User logged out: {}", username);
                if (token.equals(this.sessionToken)) {
                    logger.debug("Clearing session token for user: {}", username);
                    this.sessionToken = null;
                }
            } else {
                logger.warn("Logout attempt with invalid or expired token: {}...", 
                    token.substring(0, Math.min(8, token.length())));
            }
        } else {
            logger.warn("Logout called with null token");
        }
    }

    /**
     * Registers a new user with the system.
     *
     * @param user The user to register
     * @return The registered user with updated ID
     * @throws InvalidInputException if the user data is invalid
     */
    public User registerUser(User user) throws InvalidInputException {
        logger.debug("Registration attempt for user: {}", user.getUsername());

        try {
            // Validate user data
            validateUserRegistration(user);

            // Set user ID and hash password
            user.setId(nextUserId++);
            user.setPassword(PasswordHasher.hashPassword(user.getPassword()));

            // Add to user maps
            usersByUsername.put(user.getUsername(), user);
            usersByEmail.put(user.getEmail(), user);

            // Save the updated user list
            saveUsers();

            logger.info("New user registered: {} (ID: {})", user.getUsername(), user.getId());
            return user;

        } catch (InvalidInputException e) {
            logger.warn("User registration failed - {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw new RuntimeException("Registration failed due to an unexpected error", e);
        }
    }

    /**
     * Loads users from the users_updated.txt file.
     */
    private void loadUsersFromTextFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_UPDATED_FILE))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                // Skip header line
                if (isFirstLine && line.contains("id,username,email,password,fullName")) {
                    isFirstLine = false;
                    continue;
                }

                //split the line into parts
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String username = parts[1].trim();
                        String email = parts[2].trim();
                        String password = parts[3].trim();
                        String[] nameParts = parts[4].trim().split("\\s+", 2);
                        String firstName = nameParts.length > 0 ? nameParts[0] : "";
                        String lastName = nameParts.length > 1 ? nameParts[1] : "";

                        // Create and add user
                        User user = new User(firstName, lastName, username, ' ', email);
                        user.setId(id);
                        // Hash the password when loading from text file
                        user.setPassword(PasswordHasher.hashPassword(password));

                        usersByUsername.put(username, user);
                        usersByEmail.put(email, user);

                        // Update nextUserId if needed
                        if (id >= nextUserId) {
                            nextUserId = id + 1;
                        }

                        logger.info("Loaded user: {} (ID: {})", username, id);
                    } catch (Exception e) {
                        logger.warn("Error parsing user line: {}", line, e);
                    }
                }
            }

            logger.info("Loaded {} users from text file", usersByUsername.size());

            // Save the loaded users to the serialized file for next time
            if (!usersByUsername.isEmpty()) {
                saveUsers();
            }

        } catch (IOException e) {
            logger.error("Error loading users from text file", e);
        }
    }

    /**
     * Loads users from the users_updated.txt file.
     */

    private void createDefaultAdminUser() {
        try {
            // Create a new admin user
            User admin = new User("Admin", "User", "admin", 'M', "admin@imdbclone.com");
            admin.setPassword(PasswordHasher.hashPassword("admin123"));
            admin.setId(nextUserId++);

            usersByUsername.put(admin.getUsername(), admin);
            usersByEmail.put(admin.getEmail(), admin);

            // Save the new admin user
            saveUsers();
            logger.info("Created default admin user: {}", admin.getUsername());
        } catch (Exception e) {
            logger.error("Failed to create default admin user: {}", e.getMessage(), e);
        }
    }

    /**
     * Saves the current user data to storage.
     * This method is public to allow for administrative tasks.
     */
    public void saveUsers() {
        try {
            userStorageService.saveUsers(usersByUsername, usersByEmail);
            logger.info("Saved {} users to storage", usersByUsername.size());
        } catch (Exception e) {
            logger.error("Failed to save users: {}", e.getMessage(), e);
        }
    }
    /**
     * Authenticates a user and creates a new session with detailed logging.
     *
     * @param username The username of the user
     * @param password The plaintext password
     * @return Session token
     * @throws AuthException if authentication fails
     */
    public String login(String username, String password) throws AuthException {
        logger.debug("Attempting login for user: {}", username);

        // Input validation
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login failed: Username is empty");
            throw new AuthException(
                    com.papel.imdb_clone.exceptions.AuthErrorType.INVALID_CREDENTIALS,
                    "Username is required"
            );
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Login failed: Password is empty for user: {}", username);
            throw new AuthException(
                    com.papel.imdb_clone.exceptions.AuthErrorType.INVALID_CREDENTIALS,
                    "Password is required"
            );
        }

        User user = usersByUsername.get(username);
        if (user == null) {
            logger.warn("Login failed: User not found - {}", username);
            throw new AuthException(
                    com.papel.imdb_clone.exceptions.AuthErrorType.INVALID_CREDENTIALS,
                    "Invalid username or password"
            );
        }

        logger.debug("User found, verifying password for: {}", username);

        // Verify password using secure hashing
        try {
            String storedPassword = user.getPassword();
            boolean passwordMatches = com.papel.imdb_clone.util.PasswordHasher.verifyPassword(password, storedPassword);

            if (!passwordMatches) {
                logger.warn("Login failed: Incorrect password for user - {}", username);
                throw new AuthException(
                        com.papel.imdb_clone.exceptions.AuthErrorType.INVALID_CREDENTIALS,
                        "Invalid username or password"
                );
            }
            // Log the successful password verification
            logger.debug("Password verified successfully for user: {}", username);
        } catch (Exception e) {
            logger.error("Error during password verification for user {}: {}", username, e.getMessage(), e);
            throw new AuthException(
                    com.papel.imdb_clone.exceptions.AuthErrorType.INTERNAL_ERROR,
                    "An error occurred during authentication"
            );
        }

        try {
            // Generate session token which means a unique identifier for the user session
            String sessionToken = UUID.randomUUID().toString();
            userSessions.put(sessionToken, user.getUsername());
            
            // Set the session token in the AuthService instance
            this.sessionToken = sessionToken;
            logger.info("User logged in successfully: {} with session token: {}", username, sessionToken);
            return sessionToken;
        } catch (Exception e) {
            // Log the error and throw an AuthException
            logger.error("Error during login for user {}: {}", username, e.getMessage(), e);
            throw new AuthException(
                    com.papel.imdb_clone.exceptions.AuthErrorType.INTERNAL_ERROR,
                    "An error occurred during login",
                    e
            );
        }
    }

    /**
     * Gets the currently logged-in user for a session token.
     *
     * @param sessionToken The session token
     * @return The User object if found, null otherwise
     */
    public User getCurrentUser(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            logger.debug("getCurrentUser: No session token provided");
            return null;
        }

        String username = userSessions.get(sessionToken);
        if (username == null) {
            logger.warn("getCurrentUser: No user found for session token: {}...", 
                sessionToken.substring(0, Math.min(8, sessionToken.length())));
            return null;
        }

        User user = usersByUsername.get(username.toLowerCase());
        if (user != null) {
            logger.debug("getCurrentUser: Found user '{}' with ID {} for session token: {}...", 
                username, user.getId(), sessionToken.substring(0, Math.min(8, sessionToken.length())));
            
            // Log admin status if applicable
            if (user.isAdmin()) {
                logger.info("ADMIN ACCESS: User '{}' (ID: {}) is an administrator", 
                    username, user.getId());
            }
        } else {
            logger.error("getCurrentUser: User '{}' not found in users map for session token: {}...", 
                username, sessionToken.substring(0, Math.min(8, sessionToken.length())));
        }

        return user;
    }
    
    /**
     * Gets the ID of the currently authenticated user.
     * 
     * @param sessionToken The session token of the current user
     * @return The user ID if authenticated, or -1 if not authenticated
     */
    public int getCurrentUserId(String sessionToken) {
        User user = getCurrentUser(sessionToken);
        return user != null ? user.getId() : -1;
    }

    /**
     * Registers a new user with comprehensive validation and logging.
     *
     * @param user            The user to register
     * @param password        The plaintext password
     * @param confirmPassword The password confirmation
     * @return The registered user
     * @throws AuthException         if registration fails
     * @throws InvalidInputException if input validation fails
     */
    public User register(User user, String password, String confirmPassword) throws AuthException, InvalidInputException {
        final String methodName = "register";
        final String username = user != null ? user.getUsername() : "[unknown]";

        logger.info("Starting registration for new user: {}", username);

        // Input validation
        if (user == null) {
            throw new InvalidInputException("User cannot be null");
        }

        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("Password is required");
        }

        if (!password.equals(confirmPassword)) {
            String errorMsg = "Passwords do not match";
            logger.warn("{}: {} for user: {}", methodName, errorMsg, username);
            throw new InvalidInputException(errorMsg);
        }

        // Validate user data
        validateUserRegistration(user);

        // Check for existing user
        String usernameLower = user.getUsername().toLowerCase();
        String emailLower = user.getEmail().toLowerCase();

        if (usersByUsername.containsKey(usernameLower)) {
            String errorMsg = String.format("Username '%s' is already taken", user.getUsername());
            logger.warn("{}: {}", methodName, errorMsg);
            throw new AuthException(
                    com.papel.imdb_clone.exceptions.AuthErrorType.ACCOUNT_ALREADY_EXISTS,
                    errorMsg,
                    Map.of("username", Collections.singletonList("This username is already taken")),
                    null
            );
        }

        if (usersByEmail.containsKey(emailLower)) {
            String errorMsg = String.format("Email '%s' is already registered", user.getEmail());
            logger.warn("{}: {}", methodName, errorMsg);
            throw new AuthException(
                    com.papel.imdb_clone.exceptions.AuthErrorType.ACCOUNT_ALREADY_EXISTS,
                    errorMsg,
                    Map.of("email", Collections.singletonList("This email is already registered")),
                    null
            );
        }

        // Validate password strength
        if (password.length() < MIN_PASSWORD_LENGTH) {
            String errorMsg = String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH);
            logger.warn("{}: {}", methodName, errorMsg);
            throw new InvalidInputException(errorMsg);
        }

        // Set user properties
        // Let the repository handle ID generation
        user.setId(0); // 0 indicates a new user
        user.setPassword(PasswordHasher.hashPassword(password));
        user.setActive(true);
        user.setLocked(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        try {
            // Add to in-memory collections
            usersByUsername.put(usernameLower, user);
            usersByEmail.put(emailLower, user);

            // Save to UserRepository (this will set the ID)
            User savedUser = userRepository.save(user);
            
            // Update the user object with the generated ID
            user.setId(savedUser.getId());
            
            // Update the maps with the user that has the correct ID
            usersByUsername.put(usernameLower, savedUser);
            usersByEmail.put(emailLower, savedUser);
            
            // Persist the new user
            saveUsers();
            logger.info("{}: Successfully registered user: {} (ID: {})", methodName, username, savedUser.getId());
            return savedUser;

        } catch (Exception e) {
            // Rollback in-memory changes if persistence fails
            usersByUsername.remove(usernameLower);
            usersByEmail.remove(emailLower);

            if (e instanceof AuthException) {
                logger.debug("{}: Registration failed for user {}: {}", methodName, username, e.getMessage());
                throw e;
            } else {
                String errorMsg = String.format("Unexpected error during registration for user: %s", username);
                logger.error("{}: {} - {}", methodName, errorMsg, e.getMessage(), e);
                throw new AuthException(
                        com.papel.imdb_clone.exceptions.AuthErrorType.INTERNAL_ERROR,
                        "An unexpected error occurred during registration. Please try again later.",
                        e
                );
            }
        } finally {
            logger.debug("{}: Completed registration attempt for user: {}", methodName, username);
        }
    }
}

