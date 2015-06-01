package org.bubblecloud.zigbee.lang.observe;

import java.util.Arrays;

/**
 * Wraps an Enumerated State, .
 * Provides methods which block a calling thread until a change to specified state(s) has occurred.
 * Created by Chris on 02/05/15.
 */
public final class ObservableState<TS extends Enum<TS>>
        extends Observable<StateChangeObserver<TS>> {

    private final Object monitor = new Object();

    private TS state;

    @SuppressWarnings("unchecked")
    public ObservableState(TS initialState)
    {
        super((Class<StateChangeObserver<TS>>)((Object)(StateChangeObserver.class)));
        set(initialState);
    }

    public TS get() {
        return state;
    }

    public boolean is(TS status) {
        return this.state == status;
    }

    public boolean isAnyOf(TS... statuses) {
        boolean isAny = false;
        for(TS status : statuses) {
            if( this.state == status ) {
                isAny = true;
                break;
            }
        }
        return isAny;
    }

    public void set(TS status) {
        if(this.state !=status) {
            this.state = status;
            getObserverNotifier().onStateChanged(status);
        }
    }

    public int ordinal() {
        return state.ordinal();
    }

    @SuppressWarnings("unchecked")
    public TS waitFor(TS status) {
        return waitForAnyOf(status);
    }

    public TS waitForAnyOf(TS ... statuses) {

        StateChangeObserver<TS> statusObserver = null;

        int foundStatusIndex;
        while( ( foundStatusIndex = Arrays.binarySearch(statuses, state) ) < 0) {

            if( statusObserver == null ) {
                statusObserver = new StateChangeObserver<TS>()
                {
                    @Override
                    public void onStateChanged(final TS state)
                    {
                        monitor.notifyAll();
                    }
                };

                addObserver(statusObserver);
            }

            synchronized(monitor) {
                try                           { monitor.wait(); }
                catch(InterruptedException e) { throw new RuntimeException(e); }
            }
        }

        return statuses[ foundStatusIndex ];
    }
}
