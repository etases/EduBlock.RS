package io.github.etases.edublock.rs.controller;

import io.github.etases.edublock.rs.api.controller.Controller;
import io.javalin.Javalin;

public class HelloController implements Controller {
    @Override
    public void setup(Javalin server) {
        server.get("/", ctx -> ctx.result("Hello World"));
    }
}
