package io.github.etases.edublock.rs.internal.terminal;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.RequestServer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The terminal console, which also handles the incoming terminal commands
 */
public class ServerTerminal {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTerminal.class);
    static LineReader lineReader;
    private boolean running = true;
    @Inject
    private RequestServer requestServer;

    /**
     * Init the terminal
     *
     * @throws IOException if there is an I/O error
     */
    public void init() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .jansi(true)
                .build();
        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        lineReader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        lineReader.unsetOpt(LineReader.Option.INSERT_TAB);
    }

    /**
     * Start the line reader
     */
    public void start() {
        while (running) {
            try {
                if (lineReader != null) {
                    String command = lineReader.readLine("> ");
                    if (command == null) {
                        break;
                    }
                    runCommand(command);
                }
            } catch (UserInterruptException e) {
                System.exit(0);
                return;
            } catch (EndOfFileException e) {
                return;
            } catch (Exception e) {
                LOGGER.error("Failed to read the command", e);
            }
        }
    }

    /**
     * Execute the command
     *
     * @param command the command
     */
    private void runCommand(String command) {
        String[] split = command.split(" ", 2);
        if (!requestServer.getCommandManager().handleCommand(split[0], split.length > 1 ? split[1] : "")) {
            LOGGER.warn("No command was found");
        }
    }

    /**
     * Shut down the server
     */
    public void shutdown() {
        running = false;
    }
}
