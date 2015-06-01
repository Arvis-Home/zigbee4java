package org.bubblecloud.zigbee.network.discovery;

import org.bubblecloud.zigbee.lang.observe.StateChangeObserver;
import org.bubblecloud.zigbee.util.lifecycle.LifecycleState;


/**
 * Created by Chris on 09/05/15.
 */
public interface AssociationNetworkBrowserObserver extends StateChangeObserver<LifecycleState>
{
    void onInitialNetworkBrowsingComplete();
}
