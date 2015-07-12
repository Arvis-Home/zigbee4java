package org.bubblecloud.zigbee.androidconsole.io;

import android.os.Handler;
import android.widget.ScrollView;
import android.widget.TextView;

import org.bubblecloud.zigbee.androidconsole.ui.ScrollingLogTextView;
import org.bubblecloud.zigbee.androidconsole.util.Dispatch;

import java.io.IOException;
import java.io.OutputStream;



/**
 * Created by Chris on 06/04/15.
 */
public final class ScrollingLogTextViewOutputStream extends OutputStream
{
    private final ScrollingLogTextView textView;

    public ScrollingLogTextViewOutputStream(ScrollingLogTextView textView)
    {
        this.textView = textView;
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int count) throws IOException
    {
        final String newString = new String(buffer, offset, count);

        appendTextView(newString);
    }

    @Override
    public void write(final byte[] buffer) throws IOException
    {
        final String newString = new String(buffer);

        appendTextView(newString);
    }

    @Override
    public void write(final int character) throws IOException
    {
        final String newString = new String(new char[]{(char)character});

        appendTextView(newString);
    }

    private void appendTextView(final String string)
    {
        final Runnable textViewAppender = new Runnable()
        {
            @Override
            public void run()
            {
                textView.append(string);
            }
        };

        Dispatch.onMainThread(textViewAppender);
    }
}
