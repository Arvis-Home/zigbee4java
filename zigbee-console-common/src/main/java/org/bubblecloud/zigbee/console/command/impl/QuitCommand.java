package org.bubblecloud.zigbee.console.command.impl;

import org.bubblecloud.zigbee.console.ZigBeeConsole;
import org.bubblecloud.zigbee.console.command.AbstractCommand;
import org.bubblecloud.zigbee.util.lifecycle.LifecycleState;

/**
 * Quits console.
 */
public class QuitCommand extends AbstractCommand {

    public QuitCommand() {
        super("quit", "Quits console.");
    }

    /**
     * {@inheritDoc}
     */
    public boolean process(final ZigBeeConsole console, final String[] args) {
        assert console.getState().is(LifecycleState.Started);
        console.getState().set(LifecycleState.Stopping);
        return true;
    }
}