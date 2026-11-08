package com.foodordering.exception;

/**
 * Thrown when someone tries to register an email address that already exists.
 * The controller advice turns this into an HTTP 409 (Conflict) response.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
