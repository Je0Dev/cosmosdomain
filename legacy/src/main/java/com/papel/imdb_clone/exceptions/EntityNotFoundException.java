package com.papel.imdb_clone.exceptions;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when an entity is not found in the system.
 * This is a specific type of InvalidEntityException for entity not found scenarios.
 */
public class EntityNotFoundException extends InvalidEntityException {


    private final Serializable identifier; // Changed to Serializable which means it can be transferred over the network
    private transient Class<?> entityType; // Marked as transient to avoid serialization issues
    private final String serializedEntityType; // Store class name for deserialization
    
    /**
     * Custom serialization method to handle the non-serializable Class object.
     * This method is called when the object is being serialized and stores the class name in the serializedEntityType field.
     */
    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        // Store the class name in the serializedEntityType field
        String className = entityType != null ? entityType.getName() : null;
        try {
            java.lang.reflect.Field field = getClass().getDeclaredField("serializedEntityType");
            field.setAccessible(true);
            field.set(this, className);
        } catch (Exception e) {
            throw new java.io.NotSerializableException("Failed to serialize entity type: " + e.getMessage());
        }

        //default write object
        out.defaultWriteObject();
    }
    
    /**
     * Custom deserialization method to handle the non-serializable Class object.
     * This method is called when the object is being deserialized and restores the class object from the stored name in serializedEntityType.
     */
    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Restore the class object from the stored name in serializedEntityType
        if (this.serializedEntityType != null) {
            try {
                this.entityType = Class.forName(this.serializedEntityType);
            } catch (ClassNotFoundException e) {
                this.entityType = null;
            }
        }
    }

    /**
     * Creates an exception with entity type, identifier, and cause
     */
    public EntityNotFoundException(Class<?> entityType, String entityName, Object identifier,
                                   String message, Throwable cause) {
        super(entityName,
                identifier,
                message != null ? message : createMessage(entityType, identifier),
                createFieldErrors(entityType, identifier),
                cause);
        //set entity type, identifier, and entity name
        this.entityType = entityType;
        this.identifier = (Serializable) identifier;
        String entityName1 = entityName != null ? entityName :
                (entityType != null ? entityType.getSimpleName() : null);
        // Store class name as String for serialization
        String entityTypeName = entityType != null ? entityType.getName() : null;
        this.serializedEntityType = entityType != null ? entityType.getName() : null;
    }

    /**
     * Creates a standard error message based on entity type and identifier
     */
    private static String createMessage(Class<?> entityType, Object identifier) {
        return String.format("%s not found with identifier: %s",
                entityType != null ? entityType.getSimpleName() : "Entity",
                identifier);
    }

    //create field errors
    private static Map<String, List<String>> createFieldErrors(Class<?> entityType, Object identifier) {
        //create field errors
        Map<String, List<String>> errors = new HashMap<>();
        //get field name from entity type
        String fieldName = entityType != null ?
                String.format("%sId", entityType.getSimpleName().toLowerCase()) : "id";
        errors.put(fieldName, Collections.singletonList("not found: " + identifier));
        return errors;
    }
}