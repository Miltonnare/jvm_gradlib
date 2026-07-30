package com.aura.container;

/**
 * Exception thrown when container processing fails.
 */
public class ContainerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ContainerException(String message) {
        super(message);
    }

    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
