package org.bubblecloud.zigbee.lang.observe;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;


/**
 * A concise, readable and type-safe mechanism for implementing the Observer pattern.
 * Exposes a Proxy created to the given Observer interface.
 * Calls to the Proxy get broadcast to all registered Observers.
 *
 * Created by Chris on 05/05/15.
 */
public class Observable<TO extends Observer>
{
    protected final TO observerNotifier;

    private final Set<TO> observers = new HashSet<>();

    /**
     * Construct an observable object which uses an interface-based proxy 'observerNotifier' to forward method
     * invocations to all observers.  May be used 'as is' or sub-classed for more specific handling
     * of observation interfaces.
     * @param observerInterface The observer interface.  All methods in this interface must be of return type 'void'.
      */
    public Observable(Class<TO> observerInterface)
    {
        if ( !isInterfaceValid(observerInterface) )
        {
            throw new RuntimeException();
        }

        final InvocationHandler observationHandler = new InvocationHandler()
        {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
            {
                assert proxy == Observable.this.observerNotifier;

                Observable.this.notify(method, args);

                return null;
            }
        };

        Object notifierObject = Proxy.newProxyInstance(Observable.class.getClassLoader(),
                                                       new Class[]{observerInterface},
                                                       observationHandler);

        observerNotifier = (TO) notifierObject;
    }

    /**
     * Invokes the notifying method for all observers.
     * May be overridden to implement custom behaviour for specific notifications
     * e.g. handling of returned values.
     */
    protected void notify(final Method method, final Object[] args) throws Throwable
    {
        for(TO observer : observers)
        {
            method.invoke(observer, args); // Return value is ignored
        }
    }

    /**
     * Validates an Observer interface by checking that all of it's methods return types are void.
     * May be overridden to implement specific validations e.g. re-allow return types for .
     */
    protected boolean isInterfaceValid(final Class<TO> observerInterface)
    {
        boolean isValid = true;

        for( Method method : observerInterface.getMethods() )
        {
            if( method.getReturnType() != null )
            {
                isValid = false;
                break;
            }
        }

        return isValid;
    }

    public final void addObserver(TO observer)
    {
        observers.add(observer);
    }

    public final void removeObserver(TO observer)
    {
        observers.remove(observer);
    }

    public final TO getObserverNotifier()
    {
        return observerNotifier;
    }
}
