package io.github.etases.edublock.rs.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class Command {
    /**
     * The name of the command
     */
    private final String name;

    /**
     * The aliases (or alternative names)
     */
    private List<String> aliases = Collections.emptyList();

    /**
     * Call when running the command
     *
     * @param argument the argument
     */
    public abstract void runCommand(String argument);

    public void disable() {
        // EMPTY
    }

    /**
     * Get the format string which tells how to use the command
     *
     * @return the format string
     */
    public String getUsage() {
        return name;
    }

    /**
     * Get the description of the command
     *
     * @return the description
     */
    public String getDescription() {
        return "";
    }
}
