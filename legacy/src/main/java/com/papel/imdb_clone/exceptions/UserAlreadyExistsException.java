package com.papel.imdb_clone.exceptions;

import java.io.Serial;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when attempting to create a user that already exists in the system.
 * This is a specific type of AuthException for user registration conflicts.
 */
public class UserAlreadyExistsException extends AuthException {

    
    private static final String DEFAULT_FIELD = "email"; //default field name


    /**
     * Creates a new UserAlreadyExistsException with a custom message, field details, and cause.
     *
     * @param message the detail message
     * @param field   the field that caused the conflict
     * @param value   the value that caused the conflict
     * @param cause   the cause of the exception
     */
    public UserAlreadyExistsException(String message, String field, Object value, Throwable cause) {
        super(
                AuthErrorType.ACCOUNT_ALREADY_EXISTS,
                message != null ? message : createDefaultMessage(field, value),
                createFieldErrors(field, value),
                cause
        );
    }

    //create default message
    private static String createDefaultMessage(String field, Object value) {
        //get field name from field
        String fieldName = field != null ? field : DEFAULT_FIELD;
        return String.format("User with %s '%s' already exists", fieldName, value);
    }

    //create field errors
    private static Map<String, List<String>> createFieldErrors(String field, Object value) {
        String fieldName = field != null ? field : DEFAULT_FIELD;
        //create field errors
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(fieldName, Collections.singletonList("already exists"));
        return errors;
    }
}
