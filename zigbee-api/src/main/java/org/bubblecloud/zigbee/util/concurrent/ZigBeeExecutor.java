package org.bubblecloud.zigbee.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Created by Chris on 09/05/15.
 */
public class ZigBeeExecutor
{
    public static final ScheduledExecutorService zigBeeExecutor = Executors.newSingleThreadScheduledExecutor();
}
