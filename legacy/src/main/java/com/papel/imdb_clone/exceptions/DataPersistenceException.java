package com.papel.imdb_clone.exceptions;

/**
 * Exception thrown when there is an error persisting or loading data.
 * Includes an error type for better error handling and user feedback.
 */
public class DataPersistenceException extends RuntimeException {


    private final AuthErrorType errorType;

    /**
     * Creates a new DataPersistenceException with the specified detail message and error type.
     *
     * @param message the detail message
     * @param errorType the type of authentication error
     */
    public DataPersistenceException(String message, AuthErrorType errorType) {
        super(message);
        this.errorType = errorType != null ? errorType : AuthErrorType.INTERNAL_ERROR;
    }

    /**
     * Creates a new DataPersistenceException with the specified detail message, error type, and cause.
     *
     * @param message the detail message
     * @param errorType the type of authentication error
     * @param cause the cause of the exception
     */
    public DataPersistenceException(String message, AuthErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType != null ? errorType : AuthErrorType.INTERNAL_ERROR;
    }


    @Override
    public String toString() {
        return String.format("%s [errorType=%s]", super.toString(), errorType);
    }
}
