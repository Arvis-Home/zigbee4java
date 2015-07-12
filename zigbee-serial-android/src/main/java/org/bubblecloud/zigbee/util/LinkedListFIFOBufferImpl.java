package org.bubblecloud.zigbee.util;

import java.lang.reflect.Array;
import java.util.LinkedList;

/**
 * Implementation of FIFOByteBuffer.
 * Distinguishing features vs. CircularFIFOByteBufferImpl:
 * - Can expand to any size
 * - Less memory efficient by a factor of at least 3 (each byte resides in the node of a LinkedList)
 * @author <a href="mailto:christopherhattonuk@gmail.com">Chris Hatton</a>
 */
public class LinkedListFIFOBufferImpl<T> implements FIFOBuffer<T>
{
    private final Object mutex = new Object();

    private LinkedList<T> buffer = new LinkedList<T>();

    @Override
    public void push(T value)
    {
        synchronized (mutex)
        {
            buffer.addFirst(value);
        }
    }

    @Override
    public void pushAll(T[] values)
    {
        synchronized (mutex)
        {
            for (T value : values)
                buffer.add(value);
        }
    }

    @Override
    public T pop()
    {
        synchronized (mutex)
        {
            return buffer.removeLast();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] popAll()
    {
        synchronized (mutex)
        {
            Object[] all = new Object[buffer.size()];
            int i = 0;
            for (T value : buffer)
                all[++i] = value;

            buffer.clear();
            return (T[])all;
        }
    }

    @Override
    public final int size()
    {
        synchronized (mutex)
        {
            return buffer.size();
        }
    }

    @Override
    public void clear()
    {
        synchronized (mutex)
        {
            buffer.clear();
        }
    }
}
