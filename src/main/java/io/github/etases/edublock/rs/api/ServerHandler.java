package io.github.etases.edublock.rs.api;

/**
 * The handler interface for the server
 */
public interface ServerHandler {
    /**
     * Set up the server
     */
    void setup();

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
}
