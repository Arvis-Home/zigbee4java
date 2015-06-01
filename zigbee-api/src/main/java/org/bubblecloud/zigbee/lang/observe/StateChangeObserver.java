package org.bubblecloud.zigbee.lang.observe;

/**
 * Created by Chris on 05/05/15.
 */
public interface StateChangeObserver<TS extends Enum<TS>> extends Observer
{
    void onStateChanged(TS state);
}
