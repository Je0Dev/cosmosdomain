package com.papel.imdb_clone.exceptions;

/**
 * Exception thrown when invalid input is provided to a method.
 */
public class InvalidInputException extends Exception {

    //default constructor
    public InvalidInputException() {
        super();
    }

    public InvalidInputException(String message) {
        super(message);
    }
}
