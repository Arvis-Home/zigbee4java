package org.bubblecloud.zigbee.androidconsole.io;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.bubblecloud.zigbee.androidconsole.R;
import org.bubblecloud.zigbee.util.CircularFIFOBufferImpl;
import org.bubblecloud.zigbee.util.FIFOBuffer;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Chris on 06/04/15.
 */
public final class EditTextInputStream extends InputStream
{
    final FIFOBuffer<Character> buffer = new CircularFIFOBufferImpl<Character>();

    public EditTextInputStream(EditText editText)
    {
        final TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(final TextView textView, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED)
                {
                    synchronized(buffer)
                    {
                        for(Character c : textView.getText().toString().toCharArray())
                        {
                            buffer.push(c);
                        }

                        for(Character c:System.getProperty("line.separator").toCharArray())
                        {
                            buffer.push(c);
                        }

                        buffer.notifyAll();
                    }

                    return true;
                }
                return false;
            }
        };

        editText.setOnEditorActionListener(actionListener);
    }

    @Override
    public int read() throws IOException
    {
        synchronized(buffer)
        {
            while(buffer.size() == 0)
            {
                try
                {
                    buffer.wait();
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            Character c = buffer.pop();

            return c;
        }
    }
}
