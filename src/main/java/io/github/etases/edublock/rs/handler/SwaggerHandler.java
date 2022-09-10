package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.core.JavalinConfig;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.javalin.plugin.openapi.utils.OpenApiVersionUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

public class SwaggerHandler extends SimpleServerHandler {
    @Inject
    public SwaggerHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    public void setup() {
        OpenApiVersionUtil.INSTANCE.setLogWarnings(false);
        super.setup();
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        config.registerPlugin(new OpenApiPlugin(
                new OpenApiOptions(() -> {
                    Info info = new Info()
                            .version(getClass().getPackage().getImplementationVersion())
                            .description("EduBlock Request Server");
                    Components components = new Components().addSecuritySchemes(
                            "bearerAuth",
                            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                    );
                    return new OpenAPI().info(info).components(components);
                })
                        .path("/swagger-docs")
                        .swagger(
                                new SwaggerOptions("/swagger")
                                        .title("Edublock Request Server Documentation")
                        )
        ));
    }
}
