package io.github.etases.edublock.rs.api.controller;

import io.javalin.Javalin;

/**
 * The controller, which handles the requests
 */
public interface Controller {
    /**
     * Set up the endpoints to the server.
     * Used to register different request methods specialized for the relevant controller.
     *
     * @param server the server
     */
    void setup(Javalin server);
}
