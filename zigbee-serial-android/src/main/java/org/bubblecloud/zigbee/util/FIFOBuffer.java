package org.bubblecloud.zigbee.util;

/**
 * @author <a href="mailto:christopherhattonuk@gmail.com">Chris Hatton</a>
 */
public interface FIFOBuffer<T>
{
    void   push(T value);
    void   pushAll(T[] values);

    T      pop();
    T[]    popAll();

    int    size();
    void   clear();
}
