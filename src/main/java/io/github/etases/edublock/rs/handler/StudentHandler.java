package io.github.etases.edublock.rs.handler;

import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.model.output.AccountListResponse;
import io.github.etases.edublock.rs.model.output.StudentRequestValidationResponse;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

public class StudentHandler extends SimpleServerHandler {

    private final SessionFactory sessionFactory;

    @Inject
    public StudentHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.post("/student/requestValidation", new StudentRequestRecordValidation().handler(), JwtHandler.Roles.STUDENT);
    }

    private class StudentRequestRecordValidation implements ContextHandler {

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", StudentRequestValidationResponse.class, builder -> builder.description("Student request validation"));
        }

        @Override
        public void handle(Context ctx) {

        }
    }
}
