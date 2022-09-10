package io.github.etases.edublock.rs.internal.terminal;

import org.tinylog.core.LogEntry;
import org.tinylog.writers.AbstractFormatPatternWriter;

import java.util.Map;

import static io.github.etases.edublock.rs.internal.terminal.ServerTerminal.lineReader;

/**
 * The customized writer for {@link ServerConsoleWriter}.
 */
public class ServerConsoleWriter extends AbstractFormatPatternWriter {
    public ServerConsoleWriter(Map<String, String> properties) {
        super(properties);
    }

    @Override
    public void write(LogEntry logEntry) {
        if (lineReader != null) {
            lineReader.printAbove(render(logEntry));
        } else {
            System.out.print(render(logEntry));
        }
    }

    @Override
    public void flush() {
        // EMPTY
    }

    @Override
    public void close() {
        // EMPTY
    }
}
