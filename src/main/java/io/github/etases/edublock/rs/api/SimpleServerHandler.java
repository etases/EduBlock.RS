package io.github.etases.edublock.rs.api;

import io.github.etases.edublock.rs.ServerBuilder;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

/**
 * A simple server handler for {@link JavalinConfig} and {@link Javalin}
 */
public class SimpleServerHandler implements ServerHandler {
    private final ServerBuilder serverBuilder;

    public SimpleServerHandler(ServerBuilder serverBuilder) {
        this.serverBuilder = serverBuilder;
    }

    @Override
    public void setup() {
        serverBuilder.addHandler(this::setupServer).addConfig(this::setupConfig);
    }

    /**
     * Set up the server configuration
     *
     * @param config the configuration
     */
    protected void setupConfig(JavalinConfig config) {
        // EMPTY
    }

    /**
     * Set up the server instance
     *
     * @param server the server instance
     */
    protected void setupServer(Javalin server) {
        // EMPTY
    }
}
