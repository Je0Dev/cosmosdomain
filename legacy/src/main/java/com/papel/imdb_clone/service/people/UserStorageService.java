package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.exceptions.AuthErrorType;
import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.exceptions.DataPersistenceException;
import com.papel.imdb_clone.model.people.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service responsible for persisting and loading user data to/from disk.
 * Implements thread-safe operations with atomic file writes and proper error handling.
 */
public class UserStorageService {

    private static final String CLASS_NAME = UserStorageService.class.getSimpleName();
    
    // Configuration
    private static final String DATA_DIR = "data";
    private static final String USER_DATA_FILE = DATA_DIR + "/user_data.ser";

    // Singleton instance
    private static volatile UserStorageService instance;
    private static final Object instanceLock = new Object();
    
    // Thread safety
    private final AtomicBoolean isSaving = new AtomicBoolean(false);
    
    // Logging
    private final Logger logger;
    
    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the logger and ensures data directory exists.
     */
    private UserStorageService() {
        // Initialize logger
        this.logger = Logger.getLogger(CLASS_NAME);
        logger.setLevel(Level.INFO);
        
        // Configure logger to use console handler if no handlers are present
        if (logger.getHandlers().length == 0) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);
        }
        
        // Ensure data directory exists
        ensureDataDirectory();
    }

    /**
     * Returns the singleton instance of UserStorageService.
     * Uses double-checked locking for thread safety.
     * 
     * @return The singleton instance of UserStorageService
     */
    public static UserStorageService getInstance() {
        UserStorageService result = instance;
        if (result == null) {
            synchronized (instanceLock) {
                result = instance;
                if (result == null) {
                    instance = result = new UserStorageService();
                }
            }
        }
        return result;
    }


    /**
     * Ensures the data directory exists and is writable.
     * 
     * @throws DataPersistenceException if the directory cannot be created or is not writable
     */
    private void ensureDataDirectory() {
        Path dataDir = Paths.get(DATA_DIR);
        try {
            if (!Files.exists(dataDir)) {
                logger.info("Creating data directory: " + dataDir.toAbsolutePath());
                Files.createDirectories(dataDir);
            }
            
            // Verify directory is writable
            if (!Files.isWritable(dataDir)) {
                throw new DataPersistenceException(
                    "Data directory is not writable: " + dataDir.toAbsolutePath(),
                    AuthErrorType.STORAGE_ERROR
                );
            }
        } catch (IOException e) {
            throw new DataPersistenceException(
                "Failed to create data directory: " + e.getMessage(),
                AuthErrorType.STORAGE_ERROR,
                e
            );
        }
    }
    
    /**
     * Saves user data to disk with atomic file operations to prevent corruption.
     * Uses a temporary file and atomic move for data consistency.
     * 
     * @param usersByUsername Map of usernames to User objects (must not be null)
     * @param usersByEmail Map of emails to User objects (must not be null)
     * @throws IllegalArgumentException if either map parameter is null
     * @throws DataPersistenceException if there's an error saving the data
     * @throws AuthException if there's an authentication-related error
     */
    public void saveUsers(Map<String, User> usersByUsername, Map<String, User> usersByEmail) {
        final String methodName = "saveUsers";
        logger.entering(CLASS_NAME, methodName);
        
        // Input validation
        Objects.requireNonNull(usersByUsername, "usersByUsername map cannot be null");
        Objects.requireNonNull(usersByEmail, "usersByEmail map cannot be null");
        
        // Prevent concurrent saves
        if (!isSaving.compareAndSet(false, true)) {
            logger.warning("Save operation already in progress");
            throw new DataPersistenceException(
                "Another save operation is already in progress",
                AuthErrorType.CONCURRENT_MODIFICATION
            );
        }
        
        final long startTime = System.nanoTime();
        final int userCount = usersByUsername.size();
        
        // Data consistency check
        if (userCount != usersByEmail.size()) {
            String errorMsg = String.format("Data inconsistency: %d usernames vs %d emails", 
                userCount, usersByEmail.size());
            logger.severe(errorMsg);
            throw new IllegalStateException("Inconsistent user data: username and email maps have different sizes");
        }

        logger.log(Level.INFO, "Saving {0} users to file: {1}", 
                  new Object[]{userCount, USER_DATA_FILE});
        
        Path tempFile = null;
        try {
            // Prepare file paths
            Path targetPath = Paths.get(USER_DATA_FILE);
            Path tempPath = Paths.get(USER_DATA_FILE + ".tmp" + System.currentTimeMillis());
            
            // Ensure parent directory exists
            ensureDataDirectory();
            
            // Create temporary file with proper permissions
            tempFile = Files.createFile(tempPath);
            
            // Serialize data to temporary file
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(tempFile)))) {
                
                // Create defensive copies to prevent concurrent modification
                Map<String, User> usersCopy = new ConcurrentHashMap<>(usersByUsername);
                Map<String, User> emailsCopy = new ConcurrentHashMap<>(usersByEmail);
                
                oos.writeObject(usersCopy);
                oos.writeObject(emailsCopy);
                oos.flush();
                
                // Force OS to write data to disk
                oos.flush();
                oos.close();
                
                // Ensure all bytes are written to disk
                Files.getFileStore(tempFile).getUsableSpace();
                
                logger.fine("User data successfully written to temporary file");
                
            } catch (NotSerializableException e) {
                throw new DataPersistenceException(
                    "User object contains non-serializable data: " + e.getMessage(),
                    AuthErrorType.SERIALIZATION_ERROR,
                    e
                );
            } catch (IOException e) {
                throw new DataPersistenceException(
                    "Failed to write user data to temporary file: " + e.getMessage(),
                    AuthErrorType.IO_ERROR,
                    e
                );
            }
            
            // Atomically replace the old file with the new one
            try {
                Files.move(
                    tempPath, 
                    targetPath, 
                    StandardCopyOption.REPLACE_EXISTING, 
                    StandardCopyOption.ATOMIC_MOVE
                );
                
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                logger.log(Level.INFO, "Successfully saved {0} users in {1} ms", 
                          new Object[]{userCount, durationMs});
                
            } catch (AtomicMoveNotSupportedException e) {
                // Fallback to non-atomic move if atomic move is not supported
                logger.warning("Atomic move not supported, falling back to non-atomic move");
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            
        } catch (FileAlreadyExistsException e) {
            throw new DataPersistenceException(
                "Temporary file already exists, another save operation might be in progress",
                AuthErrorType.CONCURRENT_MODIFICATION,
                e
            );
        } catch (AccessDeniedException e) {
            throw new AuthException(
                AuthErrorType.PERMISSION_DENIED,
                "Insufficient permissions to write to the data directory: " + e.getMessage(),
                e
            );
        } catch (NoSuchFileException e) {
            throw new DataPersistenceException(
                "Parent directory does not exist and could not be created",
                AuthErrorType.STORAGE_ERROR,
                e
            );
        } catch (IOException e) {
            throw new DataPersistenceException(
                "Failed to save user data: " + e.getMessage(),
                AuthErrorType.IO_ERROR,
                e
            );
        } finally {
            // Clean up temporary file if it exists and wasn't moved
            if (tempFile != null && Files.exists(tempFile)) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to delete temporary file: " + e.getMessage(), e);
                }
            }
            
            // Release the lock
            isSaving.set(false);
            logger.exiting(CLASS_NAME, methodName);
        }
    }

    /**
     * Loads user data from the file into the provided maps.
     * This method is thread-safe and performs validation on the loaded data.
     *
     * @param usersByUsername Map to be populated with usernames as keys and User objects as values (must not be null)
     * @param usersByEmail Map to be populated with emails as keys and User objects as values (must not be null)
     * @throws IllegalArgumentException if either map parameter is null
     * @throws SecurityException if a security manager exists and denies file operations
     * @throws RuntimeException if there's an error loading or validating the data
     */
    @SuppressWarnings("unchecked")
    public void loadUsers(Map<String, User> usersByUsername, Map<String, User> usersByEmail) {
        Objects.requireNonNull(usersByUsername, "usersByUsername map cannot be null");
        Objects.requireNonNull(usersByEmail, "usersByEmail map cannot be null");

        final long startTime = System.nanoTime();
        logger.fine("Starting to load user data from file");

        Path dataFile = Paths.get(USER_DATA_FILE);
        if (!Files.exists(dataFile)) {
            logger.info("No existing user data file found at " + dataFile.toAbsolutePath() + ", starting with empty user store");
            return;
        }

        // Verify file is not empty
        try {
            if (Files.size(dataFile) == 0) {
                logger.warning("User data file is empty");
                return;
            }
        } catch (IOException e) {
            String errorMsg = "Error checking user data file size: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }

        // Load user data from file
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(dataFile)))) {
            
            // Read the list of maps from the file
            Object dataObj = ois.readObject();
            if (!(dataObj instanceof List)) {
                throw new IOException("Invalid data format: expected List but got " + 
                    (dataObj != null ? dataObj.getClass().getName() : "null"));
            }

            // Load the data from the file
            List<?> data = (List<?>) dataObj;
            if (data.size() < 2) {
                throw new IOException("Invalid data format: expected list of size 2 but got " + data.size());
            }
            
            // Get the maps from the loaded data with type safety
            Map<String, User> loadedUsersByUsername = safeCastMap(data.get(0), "usersByUsername");
            Map<String, User> loadedUsersByEmail = safeCastMap(data.get(1), "usersByEmail");
            
            // Validate the loaded data
            validateUserMaps(loadedUsersByUsername, loadedUsersByEmail);
            
            // Clear the existing maps and add the loaded data
            usersByUsername.clear();
            usersByEmail.clear();
            usersByUsername.putAll(loadedUsersByUsername);
            usersByEmail.putAll(loadedUsersByEmail);
            
            // Log successful load
            long durationNanos = System.nanoTime() - startTime;
            double durationMs = durationNanos / 1_000_000.0;
            
            logger.log(Level.INFO, "Successfully loaded {0} users in {1} ms", 
                new Object[]{usersByUsername.size(), String.format("%.2f", durationMs)});
            
        } catch (ClassNotFoundException e) {

            String errorMsg = "Error loading user data: Invalid class in data file - " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (InvalidClassException e) {

            String errorMsg = "Version mismatch in serialized user data: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (IOException e) {

            String errorMsg = "Error reading user data file: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (SecurityException e) {
            String errorMsg = "Security manager denied access to load user data: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw e;
        } catch (Exception e) {
            String errorMsg = "Unexpected error loading user data: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Safely casts an object to Map<String, User> with proper error handling.
     * 
     * @param obj The object to cast
     * @param mapName The name of the map for error messages
     * @return The casted map which means the map is now of type Map<String, User>
     * @throws IOException If the object cannot be cast to Map<String, User>
     */
    @SuppressWarnings("unchecked")
    private Map<String, User> safeCastMap(Object obj, String mapName) throws IOException {
        if (!(obj instanceof Map)) {
            throw new IOException(String.format("Invalid data format: %s is not a map", mapName));
        }
        
        try {
            return (Map<String, User>) obj;
        } catch (ClassCastException e) {
            throw new IOException(String.format("Invalid data format: %s has incorrect type parameters", mapName), e);
        }
    }
    
    /**
     * Validates that the loaded user maps are consistent with each other.
     * 
     * @param usersByUsername Map of usernames to users
     * @param usersByEmail Map of emails to users
     * @throws IOException If the maps are inconsistent
     */
    private void validateUserMaps(Map<String, User> usersByUsername, Map<String, User> usersByEmail) 
            throws IOException {
        
        if (usersByUsername.size() != usersByEmail.size()) {
            throw new IOException(String.format(
                "Inconsistent user data: usersByUsername has %d entries, usersByEmail has %d entries",
                usersByUsername.size(), usersByEmail.size()));
        }
        
        // Verify that all users in usersByUsername have corresponding entries in usersByEmail
        for (Map.Entry<String, User> entry : usersByUsername.entrySet()) {
            User user = entry.getValue();
            if (user == null) {
                throw new IOException("Null user found in usersByUsername for key: " + entry.getKey());
            }
            
            String email = user.getEmail();
            if (email == null || email.trim().isEmpty()) {
                throw new IOException("User " + entry.getKey() + " has no email address");
            }
            
            User emailUser = usersByEmail.get(email);
            if (emailUser == null) {
                throw new IOException("No email entry found for user: " + entry.getKey());
            }
            
            if (emailUser != user) {
                throw new IOException("User reference mismatch between username and email maps for: " + entry.getKey());
            }
        }
    }
}
