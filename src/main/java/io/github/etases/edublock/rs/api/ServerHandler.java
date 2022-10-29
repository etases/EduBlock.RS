package io.github.etases.edublock.rs.api;

/**
 * The handler interface for the server
 */
public interface ServerHandler {
    /**
     * Set up the server
     */
    default void setup() {
        // Do nothing
    }

    /**
     * Post set up the server
     */
    default void postSetup() {
        // Do nothing
    }

    /**
     * Stop the server
     */
    default void stop() {
        // Do nothing
    }

    /**
     * Post stop the server
     */
    default void postStop() {
        // Do nothing
    }
}
