package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.model.output.ResponseWithData;
import io.javalin.Javalin;
import io.javalin.validation.ValidationException;

public class ValidationErrorHandler extends SimpleServerHandler {
    @Inject
    public ValidationErrorHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupServer(Javalin server) {
        server.exception(ValidationException.class, (exception, ctx) -> {
            ctx.status(400);
            ctx.json(new ResponseWithData<>(-1991, "Validation error", exception.getErrors()));
        });
        server.exception(NumberFormatException.class, (exception, ctx) -> {
            ctx.status(400);
            ctx.json(new ResponseWithData<>(-1992, "Invalid number format", exception.getMessage()));
        });
    }
}
