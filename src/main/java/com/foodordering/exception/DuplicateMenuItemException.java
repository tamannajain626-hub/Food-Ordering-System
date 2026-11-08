package com.foodordering.exception;

/**
 * Thrown when a dish with the same name is already on the menu.
 * The GlobalExceptionHandler turns it into a 409 Conflict.
 */
public class DuplicateMenuItemException extends RuntimeException {

    public DuplicateMenuItemException(String message) {
        super(message);
    }
}
