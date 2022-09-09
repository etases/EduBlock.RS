package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.api.Handler;
import io.javalin.Javalin;

public class HelloHandler implements Handler {
    @Inject
    private Javalin server;

    @Override
    public void setup() {
        server.get("/", ctx -> ctx.result("Hello World"));
    }
}
