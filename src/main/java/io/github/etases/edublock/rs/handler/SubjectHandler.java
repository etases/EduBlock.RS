package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SubjectHandler extends SimpleServerHandler {
    @Inject
    public SubjectHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/subject/list", this::list);
        server.get("/subject/{id}", this::get);
        server.post("/subject", this::add, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
    }

    private void list(Context context) {

    }

    private void get(Context context) {

    }

    private void add(Context context) {

    }
}
