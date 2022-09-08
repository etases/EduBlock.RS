package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.manager.CommandManager;
import io.github.etases.edublock.rs.terminal.ServerTerminal;
import io.javalin.Javalin;
import lombok.Getter;
import org.tinylog.Logger;

@Getter
public class RequestServer {
    private static RequestServer instance;

    private final CommandManager commandManager;
    private final ServerTerminal terminal;
    private final Javalin server;

    private RequestServer() {
        terminal = new ServerTerminal();
        commandManager = new CommandManager();
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
        server.get("/", ctx -> ctx.result("Hello World"));

        terminal.start();
    }

    public void stop() {
        server.stop();
        terminal.shutdown();
    }
}