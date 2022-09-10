package io.github.etases.edublock.rs.api;

import io.github.etases.edublock.rs.ServerBuilder;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;

public class SimpleServerHandler implements ServerHandler {
    private final ServerBuilder serverBuilder;

    public SimpleServerHandler(ServerBuilder serverBuilder) {
        this.serverBuilder = serverBuilder;
    }

    @Override
    public void setup() {
        serverBuilder.addHandler(this::setupServer).addConfig(this::setupConfig);
    }

    protected void setupConfig(JavalinConfig config) {
        // EMPTY
    }

    protected void setupServer(Javalin server) {
        // EMPTY
    }
}
