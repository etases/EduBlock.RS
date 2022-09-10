package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.core.JavalinConfig;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.javalin.plugin.openapi.utils.OpenApiVersionUtil;
import io.swagger.v3.oas.models.info.Info;

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
                new OpenApiOptions(
                        new Info()
                                .version(getClass().getPackage().getImplementationVersion())
                                .description("EduBlock Request Server")
                )
                        .path("/swagger-docs")
                        .swagger(
                                new SwaggerOptions("/swagger")
                                        .title("Edublock Request Server Documentation")
                        )
        ));
    }
}
