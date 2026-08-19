package com.papel.imdb_clone.tools;

import com.papel.imdb_clone.exceptions.UserAlreadyExistsException;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.validation.AuthService;
import com.papel.imdb_clone.util.UIUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDataRegenerator {
    private static final Logger logger = Logger.getLogger(UserDataRegenerator.class.getName());

    /**
     * Regenerates user data by reading from a file and registering users.
     * This method reads user information from a text file, creates User objects,
     * and registers them using the AuthService. It handles exceptions for existing users
     * and other registration errors.
     *
     * @param args Command line arguments (not used in this implementation)
     */
    public static void main(String[] args) {

        UIUtils.showInfo("User Data Regenerator", "Starting user data regeneration process");
        // Get the base path from classpath resources
        String usersFile = Objects.requireNonNull(UserDataRegenerator.class.getClassLoader()
                .getResource("data/people/users_updated.txt")).getFile();

        // Get the default password from the AuthService
        String defaultPassword = "Password123";


        AuthService authService = AuthService.getInstance();
        int count = 0;

        // Log the start of the user data regeneration process
        System.out.println("Loading users from: " + usersFile);
        logger.info("Starting user data regeneration process");

        // Try to read the file
        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;

            // Read each line from the file
            while ((line = br.readLine()) != null) {
                // Skip comment lines and empty lines
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                // Split the line into parts
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    String msg = String.format("Warning: Invalid line format (expected at least 5 parts), skipping: %s", line);
                    System.out.println(msg);
                    continue;
                }


                // Try to parse the line into parts
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String username = parts[1].trim();
                    String email = parts[2].trim();
                    String password = parts[3].trim();
                    String fullName = parts[4].trim();
                    
                    // Log the user being processed
                    String logMsg = String.format("Processing user - ID: %d, Username: %s, Email: %s", 
                            id, username, email);
                    System.out.println(logMsg);
                    logger.info(logMsg);
                    
                    // Split full name into first and last name
                    String[] nameParts = fullName.split("\\s+", 2);
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";
                    
                    // Create and register user with the provided password
                    User user = new User(firstName, lastName, username, 'U', email);
                    user.setId(id);  // Set the ID from the file
                    user.setPassword(password);  // This will hash the password
                    
                    try {
                        // Register the user through AuthService
                        authService.register(user, password, password);
                        count++;
                        
                        String successMsg = String.format("Successfully registered user - ID: %d, Username: %s", 
                                id, username);
                        System.out.println(successMsg);
                        logger.info(successMsg);
                    } catch (UserAlreadyExistsException e) {
                        String msg = String.format("User already exists - ID: %d, Username: %s", id, username);
                        System.out.println(msg);
                        logger.warning(msg);
                    } catch (Exception e) {
                        String msg = String.format("Failed to register user - ID: %d, Username: %s, Error: %s", 
                                id, username, e.getMessage());
                        System.out.println(msg);
                        logger.log(Level.SEVERE, msg, e);
                    }
                } catch (NumberFormatException e) {
                    String msg = String.format("Invalid ID format in line: %s, Error: %s", line, e.getMessage());
                    System.out.println(msg);
                    logger.warning(msg);
                }
            }

            // Log the completion message which means the number of users that were successfully registered
            String completionMsg = String.format("[INFO] User registration complete. Successfully registered %d users.", count);
            System.out.println(completionMsg);
            
        } catch (IOException e) {
            // Log the error message which means the file was not found or could not be read
            String errorMsg = "[ERROR] Failed to read users_updated.txt: " + e.getMessage();
            System.out.println(errorMsg);
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
        // Log the completion message which means the user data regeneration process is complete
        String completionMsg = String.format("[INFO] User data regeneration complete. Successfully registered %d users.", count);
        System.out.println(completionMsg);
        logger.info(completionMsg);
    }
}
