package com.foodordering.exception;

/**
 * Thrown when the client asks for something that is not in the database
 * (for example a dish id that does not exist).
 * The GlobalExceptionHandler turns it into a 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
