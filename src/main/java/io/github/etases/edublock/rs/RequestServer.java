package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.handler.*;
import io.github.etases.edublock.rs.internal.terminal.ServerTerminal;
import io.javalin.Javalin;
import lombok.Getter;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.hscore.config.simpleconfiguration.SimpleConfig;
import org.simpleyaml.configuration.file.YamlFile;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Getter
public class RequestServer {
    private final CommandManager commandManager;
    private final ServerBuilder serverBuilder;
    private final MainConfig mainConfig;
    private final DatabaseManager databaseManager;
    private final DependencyManager dependencyManager;
    private final ServerTerminal terminal;
    private Javalin server;

    RequestServer() {
        mainConfig = ConfigGenerator.newInstance(MainConfig.class,
                new SimpleConfig<>(new File(".", "config.yml"), new YamlFile(), (file, yamlFile) -> {
                    yamlFile.setConfigurationFile(file);
                    try {
                        yamlFile.loadWithComments();
                    } catch (IOException e) {
                        Logger.warn(e);
                    }
                }));
        commandManager = new CommandManager();
        serverBuilder = new ServerBuilder();
        databaseManager = new DatabaseManager(this);
        dependencyManager = new DependencyManager(this);
        terminal = dependencyManager.getInjector().getInstance(ServerTerminal.class);
    }

    public static void main(String[] args) {
        SysOutErrRedirect.init();
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

        server = serverBuilder.build();
        server.start(mainConfig.getServerProperties().host(), mainConfig.getServerProperties().port());
        terminal.start();
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
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
                ValidationErrorHandler.class,
                HelloHandler.class,
                JwtHandler.class,
                SwaggerHandler.class,
                AccountHandler.class,
                ClassroomHandler.class,
                StaffHandler.class
        );
    }
}
