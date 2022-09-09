package io.github.etases.edublock.rs.command;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.RequestServer;
import io.github.etases.edublock.rs.api.Command;
import org.tinylog.Logger;

/**
 * The command to stop the server
 */
public class StopCommand extends Command {
    @Inject
    private RequestServer requestServer;

    public StopCommand() {
        super("stop");
    }

    @Override
    public void runCommand(String argument) {
        Logger.info("Shutting down!");
        requestServer.stop();
    }

    @Override
    public String getDescription() {
        return "Stop the server";
    }
}
