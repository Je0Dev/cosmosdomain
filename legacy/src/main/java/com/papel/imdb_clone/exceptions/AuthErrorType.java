package com.papel.imdb_clone.exceptions;

/**
 * Enumerates the different types of authentication and authorization errors.
 */
public enum AuthErrorType {

    //Enums for authentication and authorization errors
    INVALID_CREDENTIALS("Invalid username or password"),
    USER_NOT_FOUND("User not found"),
    UNAUTHORIZED("Unauthorized access"),
    INTERNAL_ERROR("Internal server error"),
    ACCOUNT_ALREADY_EXISTS("An account with this email or username already exists"),
    SERIALIZATION_ERROR("Error processing data"),
    IO_ERROR("Error accessing storage"),
    STORAGE_ERROR("Storage system error"),
    PERMISSION_DENIED("Insufficient permissions"),
    CONCURRENT_MODIFICATION("Data was modified by another process, please try again"),
    DATA_ACCESS_ERROR("Error accessing data");

    private final String defaultMessage;

    AuthErrorType(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
