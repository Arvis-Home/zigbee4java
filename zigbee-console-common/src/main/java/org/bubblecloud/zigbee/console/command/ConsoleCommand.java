package org.bubblecloud.zigbee.console.command;

import org.bubblecloud.zigbee.console.ZigBeeConsole;


/**
 * Interface for console commands.
 */
public interface ConsoleCommand {

    /**
     * Get command name.
     * @return the command name
     */
    String getName();

    /**
     * Get command description.
     * @return the command description
     */
    String getDescription();

    /**
     * Get command syntax.
     * @return the command syntax
     */
    String getSyntax();

    /**
     *
     * @param console
     * @param args
     * @return
     */
    boolean process(final ZigBeeConsole console, final String[] args);
}