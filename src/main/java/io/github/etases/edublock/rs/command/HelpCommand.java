package io.github.etases.edublock.rs.command;

import io.github.etases.edublock.rs.RequestServer;
import io.github.etases.edublock.rs.api.command.Command;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The command to display all available commands
 */
public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void runCommand(String argument) {
        Logger.info("Available commands: ");
        int usageLength = 0;
        int descLength = 0;
        List<Command> commands = new ArrayList<>(RequestServer.getInstance().getCommandManager().getCommands());
        commands.sort(Comparator.comparing(Command::getName));
        for (Command command : commands) {
            usageLength = Math.max(usageLength, command.getUsage().length());
            descLength = Math.max(descLength, command.getDescription().length());
        }
        String format = "%-" + usageLength + "s\t%-" + descLength + "s";
        for (Command command : commands) {
            Logger.info(() -> String.format(format, command.getUsage(), command.getDescription()));
        }
    }

    @Override
    public String getDescription() {
        return "Help Command";
    }
}
