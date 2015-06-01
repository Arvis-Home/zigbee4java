package org.bubblecloud.zigbee.util.lifecycle;

import org.bubblecloud.zigbee.lang.observe.StateChangeObserver;
import org.bubblecloud.zigbee.util.TimePeriod;

import java.util.concurrent.ScheduledExecutorService;


/**
 * Object which has a start/stop lifecycle and, while started, performs a task at regular intervals.
 * Concrete implementations in zigbee4java are: LinkQualityIndicatorNetworkBrowser, AssociationNetworkBrowser.
 * Created by Chris on 10/05/15.
 */
public abstract class AbstractPeriodicLifecycleObject<TO extends StateChangeObserver<LifecycleState>>
        extends AbstractLifecycleObject<TO>
{
    protected final ScheduledExecutorService scheduledExecutorService;

    public AbstractPeriodicLifecycleObject(final Class<TO> observerInterface, ScheduledExecutorService scheduledExecutorService)
    {
        super(observerInterface);

        assert scheduledExecutorService != null;

        this.scheduledExecutorService = scheduledExecutorService;
    }

    protected abstract TimePeriod getNextTimePeriod();
    protected abstract Runnable   getNextPeriodTask();



    @Override
    protected void startImpl()
    {

    }

    @Override
    protected void stopImpl()
    {

    }
}
