package org.bubblecloud.zigbee.util;

import java.util.concurrent.TimeUnit;


/**
 * Created by Chris on 10/05/15.
 */
public class TimePeriod
{
    public final TimeUnit timeUnit;
    public final int      units;

    public TimePeriod(final int units, final TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        this.units    = units;
    }

    public String toString() {
        return units + " " + timeUnit.toString();
    }
}
