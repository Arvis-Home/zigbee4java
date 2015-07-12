package org.bubblecloud.zigbee.util;

/**
 * Implementation of FIFOByteBuffer.
 * Distinguishing features vs. LinkedListFIFOByteBufferImpl:
 * - Has a fixed size (and will throw an exception if the size is exceeded)
 * - Faster, more memory efficient (backed by simple byte array)
 * @author <a href="mailto:christopherhattonuk@gmail.com">Chris Hatton</a>
 */
public class CircularFIFOBufferImpl<T> implements FIFOBuffer<T>
{
    public static final int DEFAULT_CAPACITY = 512;

    private final Object mutex = new Object();

    private final T[] buffer;
    private int position, length;

    public CircularFIFOBufferImpl()
    {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public CircularFIFOBufferImpl(int capacity)
    {
        buffer = (T[])new Object[capacity];
        clear();
    }

    public final int size()
    {
        synchronized (mutex)
        {
            return length;
        }
    }

    public void push(T value)
    {
        synchronized(mutex)
        {
            if (isFull())
                throw new RuntimeException("FIFOBuffer full.");
            else
            {
                int index = (position + length) % buffer.length;
                System.out.print(index);
                buffer[index] = value;
                ++length;
            }
        }
    }

    public void pushAll(T[] values, int start, int length)
    {
        synchronized(mutex)
        {
            if(length<0 || start<0 || start+length>values.length)
                throw new IllegalArgumentException();

            if(length==0)
                return;

            for(int i=0;i<length;++i)
                push(values[start+i]);
        }
    }

    public void pushAll(T[] values)
    {
        synchronized(mutex)
        {
            for (T value : values)
                push(value);
        }
    }

    public T pop()
    {
        synchronized(mutex)
        {
            T value;
            if (length == 0)
                throw new RuntimeException();
            else
            {
                value = buffer[position];

                if (++position >= buffer.length) position = 0;
                --length;
            }
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    public T[] popAll()
    {
        synchronized(mutex)
        {
            T[] all = (T[])(new Object[length]);
            for (int i = 0; i < length; ++i)
                all[i] = pop();

            clear();

            return all;
        }
    }

    public final void clear()
    {
        synchronized (mutex)
        {
            position = length = 0;
        }
    }

    public final boolean isEmpty()
    {
        synchronized (mutex)
        {
            return length == 0;
        }
    }

    public final boolean isFull()
    {
        synchronized (mutex)
        {
            return length == buffer.length;
        }
    }
}
