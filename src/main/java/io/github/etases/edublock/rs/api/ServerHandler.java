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
     * Stop the server
     */
    default void stop() {
        // Do nothing
    }
}
