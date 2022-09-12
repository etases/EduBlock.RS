package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

public class HelloHandler extends SimpleServerHandler {
    @OpenApi(
            description = "My Operation",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Hello World",
                            content = @OpenApiContent(type = "text/plain")
                    )
            }
    )
    private final Handler sayHello = ctx -> ctx.result("Hello World");

    @Inject
    public HelloHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/", sayHello);

        server.get("/hellouser", OpenApiBuilder.documented(
                OpenApiBuilder.document()
                        .operation(SwaggerHandler.addSecurity()),
                ctx ->  {
                    ctx.result("Hello User");
                }
        ), JwtHandler.Roles.USER);
    }
}
