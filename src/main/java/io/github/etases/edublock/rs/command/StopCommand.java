package io.github.etases.edublock.rs.command;

import io.github.etases.edublock.rs.api.command.Command;
import org.tinylog.Logger;

import static io.github.etases.edublock.rs.RequestServer.getInstance;

/**
 * The command to stop the server
 */
public class StopCommand extends Command {
    public StopCommand() {
        super("stop");
    }

    @Override
    public void runCommand(String argument) {
        Logger.info("Shutting down!");
        getInstance().stop();
    }

    @Override
    public String getDescription() {
        return "Stop the server";
    }
}
