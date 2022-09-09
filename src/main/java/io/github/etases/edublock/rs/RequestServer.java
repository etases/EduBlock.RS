package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.handler.CommandHandler;
import io.github.etases.edublock.rs.handler.HelloHandler;
import io.github.etases.edublock.rs.handler.JwtHandler;
import io.github.etases.edublock.rs.jwt.JWTAccessManager;
import io.github.etases.edublock.rs.terminal.ServerTerminal;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.javalin.plugin.openapi.utils.OpenApiVersionUtil;
import io.swagger.v3.oas.models.info.Info;
import lombok.Getter;
import org.tinylog.Logger;

import java.util.List;

@Getter
public class RequestServer {
    private final CommandManager commandManager;
    private final DependencyManager dependencyManager;
    private final ServerTerminal terminal;
    private final Javalin server;

    private RequestServer() {
        server = Javalin.create(this::configServer);
        commandManager = new CommandManager();
        dependencyManager = new DependencyManager(this);
        terminal = dependencyManager.getInjector().getInstance(ServerTerminal.class);
    }

    public static void main(String[] args) {
        new RequestServer().start();
    }

    public void start() {
        try {
            terminal.init();
        } catch (Exception e) {
            Logger.error("Failed to initialize server terminal", e);
            return;
        }

        getHandlers().forEach(clazz -> dependencyManager.getInjector().getInstance(clazz).setup());

        server.start(7070);
        terminal.start();
    }

    public void stop() {
        server.stop();
        terminal.shutdown();
        commandManager.disable();
    }

    /**
     * Get the list of handlers to use in the server
     *
     * @return the list of handlers
     */
    private List<Class<? extends ServerHandler>> getHandlers() {
        return List.of(
                CommandHandler.class,
                HelloHandler.class,
                JwtHandler.class
        );
    }

    /**
     * Configure the server
     *
     * @param config the server configuration
     */
    private void configServer(JavalinConfig config) {
        OpenApiVersionUtil.INSTANCE.setLogWarnings(false);
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

        config.accessManager(new JWTAccessManager("role", Roles.getRoleMapping(), Roles.ANYONE));
    }
}