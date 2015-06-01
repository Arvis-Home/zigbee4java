package org.bubblecloud.zigbee.util.lifecycle;

import org.bubblecloud.zigbee.lang.observe.Observable;
import org.bubblecloud.zigbee.lang.observe.ObservableState;
import org.bubblecloud.zigbee.lang.observe.StateChangeBinding;
import org.bubblecloud.zigbee.lang.observe.StateChangeObserver;

import static org.bubblecloud.zigbee.util.lifecycle.LifecycleState.*;


/**
 * Object which as an observable start/stop lifecycle and performs some setup/teardown tasks to
 * transition between those states.
 *
 * Created by Chris on 09/05/15.
 */
public abstract class AbstractLifecycleObject<TO extends StateChangeObserver<LifecycleState>> extends Observable<TO>
        implements LifecycleObject  {

    private final ObservableState<LifecycleState> observableState = new ObservableState<>(Stopped);

    /**
     * Construct an observable object which uses an interface-based proxy 'observerNotifier' to forward method
     * invocations to all observers.  May be used 'as is' or sub-classed for more specific handling
     * of observation interfaces.
     *
     * @param observerInterface The observer interface.  All methods in this interface must be of return type 'void'.
     */
    public AbstractLifecycleObject(final Class<TO> observerInterface) {
        super(observerInterface);
        boolean bound = StateChangeBinding.bindStateChangeEvents(observableState, this);

        if(!bound) throw new IllegalStateException();
    }

    @Override
    public ObservableState<LifecycleState> getState() {
        return observableState;
    }

    public final void start() {
        if(observableState.isAnyOf(Starting, Started)) throw new LifecycleException();

        observableState.set(Starting);

        startImpl();
    }

    protected abstract void startImpl();

    protected final void didStart() {
        if(!observableState.is(Starting)) throw new LifecycleException();

        observableState.set(Started);
    }

    public final void stop() {
        if(observableState.isAnyOf(Stopping, Stopped)) throw new LifecycleException();

        observableState.set(Stopping);

        stopImpl();
    }

    protected abstract void stopImpl();

    protected final void didStop() {
        if(!observableState.is(Stopping)) throw new LifecycleException();

        observableState.set(Stopped);
    }

    @Override
    protected void finalize() throws Throwable
    {
        boolean unbound = StateChangeBinding.unbindStateChangeEvents(observableState, this);
        if(!unbound) throw new IllegalStateException();

        super.finalize();
    }
}
