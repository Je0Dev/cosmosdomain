package com.papel.imdb_clone.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when a duplicate entry is detected in the system.
 * Can be used for any type of duplicate entry (username, email, content, etc.).
 */
public class DuplicateEntryException extends InvalidEntityException {


    /**
     * Creates a new DuplicateEntryException with entity and field details.
     */
    public DuplicateEntryException(String entityName, Object entityId, String fieldName, Object fieldValue) {
        super(entityName, entityId,
                String.format("Duplicate entry found for %s: %s", fieldName, fieldValue),
                createFieldErrors(fieldName, fieldValue), null);
        Logger logger = LoggerFactory.getLogger(DuplicateEntryException.class);
        logger.error("Duplicate entry found for {}", fieldName);
    }


    //create field errors
    private static Map<String, List<String>> createFieldErrors(String fieldName, Object fieldValue) {
        //create field errors
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(fieldName, Collections.singletonList("already exists: " + fieldValue));
        return errors;
    }

}
