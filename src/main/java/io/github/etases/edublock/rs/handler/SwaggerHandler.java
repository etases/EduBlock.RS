package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.javalin.config.JavalinConfig;
import io.javalin.openapi.BearerAuth;
import io.javalin.openapi.OpenApiInfo;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.SecurityConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class SwaggerHandler extends SimpleServerHandler {
    public static final String AUTH_KEY = "BearerAuth";

    @Inject
    public SwaggerHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        String deprecatedDocsPath = "/swagger-docs";

        OpenApiInfo openApiInfo = new OpenApiInfo();
        openApiInfo.setTitle("Edublock Request Server Documentation");
        openApiInfo.setDescription("EduBlock Request Server");
        openApiInfo.setVersion(Optional.ofNullable(getClass().getPackage().getImplementationVersion()).orElse("0.0.0"));

        OpenApiConfiguration openApiConfiguration = new OpenApiConfiguration();
        openApiConfiguration.setInfo(openApiInfo);
        openApiConfiguration.setSecurity(new SecurityConfiguration(Map.of(AUTH_KEY, new BearerAuth()), Collections.emptyList()));
        openApiConfiguration.setDocumentationPath(deprecatedDocsPath);
        config.plugins.register(new OpenApiPlugin(openApiConfiguration));

        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
        swaggerConfiguration.setDocumentationPath(deprecatedDocsPath);
        swaggerConfiguration.setUiPath("/swagger");
        config.plugins.register(new SwaggerPlugin(swaggerConfiguration));
    }
}
