package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiSecurity;

public class HelloHandler extends SimpleServerHandler {

    @Inject
    public HelloHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/", ctx -> ctx.result("Hello World!"));

        server.get("/helloadmin", this::helloAdmin, JwtHandler.Role.ADMIN);
    }

    @OpenApi(
            path = "/helloadmin",
            methods = HttpMethod.GET,
            security = {
                    @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY)
            }
    )
    private void helloAdmin(Context ctx) {
        ctx.result("Hello Admin");
    }
}
