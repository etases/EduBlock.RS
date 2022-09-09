package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.api.Handler;
import io.github.etases.edublock.rs.handler.CommandHandler;
import io.github.etases.edublock.rs.handler.HelloHandler;
import io.github.etases.edublock.rs.terminal.ServerTerminal;
import io.javalin.Javalin;
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
        server = Javalin.create();
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
    private List<Class<? extends Handler>> getHandlers() {
        return List.of(
                CommandHandler.class,
                HelloHandler.class
        );
    }
}