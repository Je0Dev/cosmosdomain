package com.papel.imdb_clone.exceptions;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Exception thrown when an entity fails validation due to invalid data.
 * This is a more specific type of ValidationException for entity validation.
 */
public class InvalidEntityException extends ValidationException {


    private static final String DEFAULT_MESSAGE = "Invalid entity data"; // default message

    private final String entityName; // name of the invalid entity
    private final Serializable entityId; // id of the invalid entity


    /**
     * Creates a new InvalidEntityException with entity details.
     *
     * @param entityName the name of the invalid entity (must not be null)
     * @param entityId the ID of the invalid entity (must be serializable if not null)
     * @param message the detail message (if null, a default message will be used)
     * @param fieldErrors map of field names to error messages (may be null)
     * @param cause the cause of the exception (may be null)
     * @throws NullPointerException if entityName is null
     */
    public InvalidEntityException(String entityName, Object entityId, String message,
                                Map<String, List<String>> fieldErrors, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, 
              null,  // errorCode
              fieldErrors != null ? fieldErrors : Collections.emptyMap(), 
              cause);
        
        this.entityName = Objects.requireNonNull(entityName, "Entity name cannot be null");
        this.entityId = (entityId instanceof Serializable) ? (Serializable) entityId : String.valueOf(entityId);
        
        // Add entity details
        this.addDetail("entity", this.entityName);
        if (this.entityId != null) {
            this.addDetail("entityId", this.entityId);
        }
    }

}
