package com.papel.imdb_clone.service.validation;

import java.util.regex.Pattern;


/**
 * Service for validating user input with detailed error messages.
 */
public class UserInputValidator {

    // Constants for validation patterns
    //this means that the user email must be in the format of username@domain.com
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.\\w+$");
    //this means that the username must be 3-20 characters long and can only contain letters, numbers, dots and underscores like username_123
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{3,20}$");
    //this means that the password must be at least 8 characters long and include uppercase, lowercase, number and special character like Password123!
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    
    /**
     * Constructs a new UserInputValidator instance.
     * Explicit constructor is required for proper module encapsulation.
     */
    public UserInputValidator() {
        //No initialization needed
    }

    //checks if username is valid or not
    public boolean isValidUsername(String username) {
        return username == null || !USERNAME_PATTERN.matcher(username).matches();
    }

    //checks if email is valid or not
    public boolean isValidEmail(String email) {
        return email == null || !EMAIL_PATTERN.matcher(email).matches();
    }

    //checks if password is valid or not
    public boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
