package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.api.controller.Controller;
import io.github.etases.edublock.rs.controller.HelloController;
import io.github.etases.edublock.rs.terminal.ServerTerminal;
import io.javalin.Javalin;
import lombok.Getter;
import org.tinylog.Logger;

import java.util.List;

@Getter
public class RequestServer {
    private static RequestServer instance;

    private final CommandManager commandManager;
    private final DependencyManager dependencyManager;
    private final ServerTerminal terminal;
    private final Javalin server;

    private RequestServer() {
        terminal = new ServerTerminal();
        commandManager = new CommandManager();
        dependencyManager = new DependencyManager();
        server = Javalin.create();
    }

    public static RequestServer getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        instance = new RequestServer();
        instance.start();
    }

    public void start() {
        try {
            terminal.init();
        } catch (Exception e) {
            Logger.error("Failed to initialize server terminal", e);
            return;
        }

        server.start(7070);

        getControllers().forEach(clazz -> dependencyManager.getInjector().getInstance(clazz).setup(server));

        terminal.start();
    }

    public void stop() {
        server.stop();
        terminal.shutdown();
        commandManager.disable();
    }

    /**
     * Get the list of controllers to use in the server
     *
     * @return the list of controllers
     */
    private List<Class<? extends Controller>> getControllers() {
        return List.of(
                HelloController.class
        );
    }
}