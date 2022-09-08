package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.api.command.Command;
import io.github.etases.edublock.rs.api.controller.Controller;
import io.github.etases.edublock.rs.command.HelpCommand;
import io.github.etases.edublock.rs.command.StopCommand;
import io.github.etases.edublock.rs.controller.HelloController;
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
        commandManager = new CommandManager();
        dependencyManager = new DependencyManager(this);
        terminal = dependencyManager.getInjector().getInstance(ServerTerminal.class);
        server = Javalin.create();
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

        getControllers().forEach(clazz -> dependencyManager.getInjector().getInstance(clazz).setup(server));
        getCommands().forEach(clazz -> commandManager.addCommand(dependencyManager.getInjector().getInstance(clazz)));

        server.start(7070);
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

    /**
     * Get the list of commands to use in the terminal
     *
     * @return the list of commands
     */
    private List<Class<? extends Command>> getCommands() {
        return List.of(
                HelpCommand.class,
                StopCommand.class
        );
    }
}