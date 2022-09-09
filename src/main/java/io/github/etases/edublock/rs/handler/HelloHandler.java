package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.javalin.Javalin;

public class HelloHandler implements ServerHandler {
    @Inject
    private Javalin server;

    @Override
    public void setup() {
        server.get("/", ctx -> ctx.result("Hello World"));
    }
}
