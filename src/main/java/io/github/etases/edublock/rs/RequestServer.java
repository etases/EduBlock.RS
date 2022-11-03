package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.config.SystemMainConfig;
import io.github.etases.edublock.rs.handler.*;
import io.github.etases.edublock.rs.internal.classification.ClassificationManager;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import io.github.etases.edublock.rs.internal.terminal.ServerTerminal;
import io.javalin.Javalin;
import lombok.Getter;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.hscore.config.simpleconfiguration.SimpleConfig;
import org.simpleyaml.configuration.file.YamlFile;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class RequestServer {
    private final CommandManager commandManager;
    private final ServerBuilder serverBuilder;
    private final MainConfig mainConfig;
    private final DatabaseManager databaseManager;
    private final DependencyManager dependencyManager;
    private final ServerTerminal terminal;
    private final Map<Class<? extends ServerHandler>, ServerHandler> serverHandlers = new HashMap<>();
    private Javalin server;

    RequestServer(String[] args) {
        if (System.getProperty("useSystem", "false").equalsIgnoreCase("true")) {
            mainConfig = new SystemMainConfig();
        } else {
            mainConfig = ConfigGenerator.newInstance(MainConfig.class,
                    new SimpleConfig<>(new File(".", "config.yml"), new YamlFile(), (file, yamlFile) -> {
                        yamlFile.setConfigurationFile(file);
                        try {
                            yamlFile.loadWithComments();
                        } catch (IOException e) {
                            Logger.warn(e);
                        }
                    }));
        }
        commandManager = new CommandManager();
        serverBuilder = new ServerBuilder();
        databaseManager = new DatabaseManager(this);
        dependencyManager = new DependencyManager(this);
        terminal = dependencyManager.getInjector().getInstance(ServerTerminal.class);

        // Load hard-coded managers
        SubjectManager.getSubjects();
        ClassificationManager.getClassifications();
    }

    public static void main(String[] args) {
        SysOutErrRedirect.init();
        new RequestServer(args).start();
    }

    public void start() {
        try {
            terminal.init();
        } catch (Exception e) {
            Logger.error("Failed to initialize server terminal", e);
            return;
        }

        getHandlers().forEach(clazz -> serverHandlers.put(clazz, dependencyManager.getInjector().getInstance(clazz)));
        serverHandlers.values().forEach(ServerHandler::setup);
        serverHandlers.values().forEach(ServerHandler::postSetup);

        server = serverBuilder.build();
        server.start(mainConfig.getServerProperties().host(), mainConfig.getServerProperties().port());
        terminal.start();
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
        serverHandlers.values().forEach(ServerHandler::stop);
        serverHandlers.values().forEach(ServerHandler::postStop);
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
                CorsHandler.class,
                HelloHandler.class,
                JwtHandler.class,
                SwaggerHandler.class,
                AccountHandler.class,
                ClassroomHandler.class,
                RecordHandler.class,
                SubjectHandler.class,
                DevHandler.class,
                FabricHandler.class,
                StudentUpdateHandler.class
        );
    }

    /**
     * Get the handler by the class
     *
     * @param clazz the class
     * @param <T>   the type of the handler
     * @return the handler
     */
    public <T extends ServerHandler> T getHandler(Class<T> clazz) {
        var handler = serverHandlers.get(clazz);
        if (handler == null) {
            throw new IllegalArgumentException("Handler not found: " + clazz.getName());
        }
        return clazz.cast(handler);
    }
}