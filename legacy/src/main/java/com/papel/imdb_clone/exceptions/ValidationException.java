package com.papel.imdb_clone.exceptions;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Unified exception for validation errors throughout the application.
 * Supports both simple error messages and field-level validation errors.
 */
public class ValidationException extends RuntimeException implements Serializable {

    /**
     * Map of field names to their associated error messages.
     */
    private final HashMap<String, List<String>> fieldErrors = new HashMap<>();
    
    /**
     * Map to store additional details about the validation error.
     */
    private final HashMap<String, Serializable> details = new HashMap<>();

    /**
     * Error code for the exception.
     */
    private final String errorCode;

    /**
     * Creates a new ValidationException with all possible parameters.
     *
     * @param message the detail message
     * @param errorCode the error code
     * @param fieldErrors map of field names to error messages
     * @param cause the cause of the exception
     */
    public ValidationException(String message, String errorCode,
                             Map<String, List<String>> fieldErrors,
                             Throwable cause) {
        this(message, errorCode, fieldErrors, cause, true);
        
        // Initialize field errors in the public constructor
        if (fieldErrors != null) {
            for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.fieldErrors.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
        }
    }

    /**
     * Private constructor that takes an additional dummy parameter to avoid "this" escape.
     * This should only be called from the factory method.
     */
    private ValidationException(String message, String errorCode,
                             Map<String, List<String>> fieldErrors,
                             Throwable cause, boolean dummy) {
        super(message, cause);
        // Initialize error code safely
        String safeErrorCode = errorCode != null ? errorCode : "VALIDATION_ERROR";
        try {
            Field field = getClass().getDeclaredField("errorCode");
            field.setAccessible(true);
            field.set(this, safeErrorCode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize error code", e);
        }
        // Set field errors
        if (fieldErrors != null) {
            for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.fieldErrors.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
        }
        this.errorCode = "";
    }



    /**
     * Adds a serializable detail to the exception.
     * 
     * @param key the detail key
     * @param value the detail value (must be serializable)
     * @throws IllegalArgumentException if the value is not serializable
     */
    public void addDetail(String key, Object value) {
        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException("Detail value must be serializable");
        }
        details.put(key, (Serializable) value);
    }


    /**
     * Gets the details map containing additional information about the error.
     * @return an unmodifiable map of details
     */
    public Map<String, Serializable> getDetails() {
        return Collections.unmodifiableMap(details);
    }
    
    /**
     * Custom serialization method to ensure proper serialization of the exception
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    /**
     * Custom deserialization method to ensure proper deserialization of the exception
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }


    /**
     * Builder for creating ValidationException instances.
     */
    public static class Builder implements Serializable {

        private String message; //message that describes the error
        private String errorCode;//error code that describes the error
        private final Map<String, List<String>> fieldErrors = new LinkedHashMap<>();//map of field names to their associated error messages
        private Throwable cause;//cause of the exception
        
        /**
         * Constructor for Builder.
         * Required for serialization support.
         */
        public Builder() {
            // Initialize fields if needed
            this.errorCode = "VALIDATION_ERROR";
        }

        /**
         * Adds a field error to the builder.
         *
         * @param field the field name
         * @param error the error message
         */
        public void fieldError(String field, String error) {
            if (field != null && error != null) {
                //add the error to the fieldErrors map
                this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            }
        }

        /**
         * Sets the error message.
         *
         * @param message the error message
         * @return this builder instance for method chaining
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }
    }
}
