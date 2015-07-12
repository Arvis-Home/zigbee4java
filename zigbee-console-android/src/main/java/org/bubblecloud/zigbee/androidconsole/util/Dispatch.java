package org.bubblecloud.zigbee.androidconsole.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Chris on 26/04/15.
 */
public class Dispatch
{
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void onMainThread(Runnable runnable)
    {
        if(isMainThread())
        {
            runnable.run();
        }
        else
        {
            mainHandler.post(runnable);
        }
    }

    public static boolean isMainThread()
    {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
