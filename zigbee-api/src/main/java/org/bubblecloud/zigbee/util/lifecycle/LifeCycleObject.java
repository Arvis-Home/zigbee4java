package org.bubblecloud.zigbee.util.lifecycle;

import org.bubblecloud.zigbee.lang.observe.ObservableState;


/**
 * Created by Chris on 09/05/15.
 */
public interface LifecycleObject
{
    ObservableState<LifecycleState> getState();

    void start();
    void stop();
}
