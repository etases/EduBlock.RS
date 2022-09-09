package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.etases.edublock.rs.CommandManager;
import io.github.etases.edublock.rs.api.Command;
import io.github.etases.edublock.rs.api.Handler;
import io.github.etases.edublock.rs.command.HelpCommand;
import io.github.etases.edublock.rs.command.StopCommand;

import java.util.List;

public class CommandHandler implements Handler {
    @Inject
    private CommandManager commandManager;
    @Inject
    private Injector injector;

    private List<Class<? extends Command>> getCommands() {
        return List.of(
                HelpCommand.class,
                StopCommand.class
        );
    }

    @Override
    public void setup() {
        getCommands().forEach(clazz -> commandManager.addCommand(injector.getInstance(clazz)));
    }
}
