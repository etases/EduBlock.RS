package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.core.JavalinConfig;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiUpdater;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.javalin.plugin.openapi.utils.OpenApiVersionUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;

public class SwaggerHandler extends SimpleServerHandler {
    private static final String AUTH_KEY = "bearerAuth";

    @Inject
    public SwaggerHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    public static OpenApiUpdater<Operation> addSecurity() {
        return operation -> operation.security(List.of(new SecurityRequirement().addList(AUTH_KEY)));
    }

    @Override
    public void setup() {
        OpenApiVersionUtil.INSTANCE.setLogWarnings(false);
        super.setup();
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        Info info = new Info()
                .version(getClass().getPackage().getImplementationVersion())
                .description("EduBlock Request Server");
        Components components = new Components().addSecuritySchemes(
                AUTH_KEY,
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
        );
        SwaggerOptions swaggerOptions = new SwaggerOptions("/swagger")
                .title("Edublock Request Server Documentation");

        OpenApiOptions openApiOptions = new OpenApiOptions(() -> new OpenAPI().info(info).components(components))
                .swagger(swaggerOptions)
                .path("/swagger-docs")
                .activateAnnotationScanningFor(getClass().getPackage().getName());

        config.registerPlugin(new OpenApiPlugin(openApiOptions));
    }
}
