package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

public class HelloHandler extends SimpleServerHandler {

    @Inject
    public HelloHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/", ctx -> ctx.result("Hello World!"));

        server.get("/helloadmin", OpenApiBuilder.documented(
                OpenApiBuilder.document()
                        .operation(SwaggerHandler.addSecurity()),
                ctx -> {
                    ctx.result("Hello Admin");
                }
        ), JwtHandler.Roles.ADMIN);
    }
}
