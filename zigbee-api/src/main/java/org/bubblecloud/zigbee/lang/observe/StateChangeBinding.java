package org.bubblecloud.zigbee.lang.observe;


import org.bubblecloud.zigbee.lang.tuple.Pair;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Chris on 06/05/15.
 */
public final class StateChangeBinding
{
    private static Map<Pair<?,?>, StateChangeObserver<?>> bindings = new HashMap<>();

    private StateChangeBinding() {}

    @SuppressWarnings("unchecked")
    public static <TS extends Enum<TS>, TO extends StateChangeObserver<TS>> boolean bindStateChangeEvents(final ObservableState<TS> sourceObservableState, final Observable<TO> targetObservable) {

        final Pair<ObservableState<TS>,Observable<TO>> sourceTargetPair = new Pair<>(sourceObservableState, targetObservable);
        StateChangeObserver<TS> forwardingObserver = (StateChangeObserver<TS>) bindings.get(sourceTargetPair);

        final boolean bound;

        if(forwardingObserver==null) {
            forwardingObserver = new StateChangeObserver<TS>() {
                @Override
                public void onStateChanged(final TS state) {
                    targetObservable.getObserverNotifier().onStateChanged(state);
                }
            };

            bindings.put(sourceTargetPair, forwardingObserver);

            sourceObservableState.addObserver(forwardingObserver);

            bound = true;
        } else {
            bound = false;
        }

        return bound;
    }

    @SuppressWarnings("unchecked")
    public static <TS extends Enum<TS>, TO extends StateChangeObserver<TS>> boolean unbindStateChangeEvents(final ObservableState<TS> sourceObservableState, final Observable<TO> targetObservable) {

        final Pair<ObservableState<TS>,Observable<TO>> sourceTargetPair = new Pair<>(sourceObservableState, targetObservable);
        final StateChangeObserver<TS> forwardingObserver = (StateChangeObserver<TS>) bindings.get(sourceTargetPair);

        final boolean unbound;

        if(forwardingObserver == null) {
            unbound = false;
        } else {
            sourceObservableState.removeObserver(forwardingObserver);
            unbound = true;
        }

        return unbound;
    }
}
