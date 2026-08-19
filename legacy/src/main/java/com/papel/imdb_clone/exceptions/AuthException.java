package com.papel.imdb_clone.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when authentication or authorization fails.
 * Provides detailed error information through the AuthErrorType enum.
 */
public class AuthException extends ValidationException {

    private final AuthErrorType errorType;

    //Constructor for AuthException with errorType and message
    public AuthException(AuthErrorType errorType, String message) {
        this(errorType, message, null, null);
    }

    //Constructor for AuthException with errorType, message and cause
    public AuthException(AuthErrorType errorType, String message, Throwable cause) {
        this(errorType, message, null, cause);
    }
    
    public AuthException(AuthErrorType errorType, String message, Map<String, List<String>> fieldErrors, Throwable cause) {
        super(message, 
              errorType != null ? errorType.name() : "AUTH_ERROR", 
              fieldErrors != null ? new HashMap<>(fieldErrors) : null, 
              cause);
        
        // Directly assign the final field - no method calls on 'this'
        this.errorType = errorType != null ? errorType : AuthErrorType.INTERNAL_ERROR;
    }

    /**
     * Builder for AuthException.
     * Provides a fluent API for constructing AuthException instances.
     */
    public static class Builder {
        
        private AuthErrorType errorType;
        private String message;

        /**
         * Explicit constructor for Builder.
         * Required to prevent exposure of default constructor in exported package.
         */
        public Builder() {
            // Explicit constructor to prevent default constructor warning
            this.errorType = AuthErrorType.INTERNAL_ERROR;
            this.message = "Internal server error";
        }

    }
}
