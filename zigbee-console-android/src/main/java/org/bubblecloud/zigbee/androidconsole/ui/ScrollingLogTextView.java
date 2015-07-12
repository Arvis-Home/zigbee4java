package org.bubblecloud.zigbee.androidconsole.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import org.bubblecloud.zigbee.androidconsole.R;


/**
 * Created by Chris on 29/04/15.
 */
public class ScrollingLogTextView extends FrameLayout
{
    private final ScrollView scrollView;
    private final EditText   editText;

    public ScrollingLogTextView(final Context context, AttributeSet attrs)
    {
        super(context, attrs);

        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate( R.layout.view_scrolling_log_text, this, true );

        scrollView = (ScrollView) findViewById( R.id.scrollView );
        editText   = (EditText)   findViewById( R.id.editText   );

        TypedArray a=getContext().obtainStyledAttributes(attrs, R.styleable.ScrollingLogTextView);

        String text = a.getString(R.styleable.ScrollingLogTextView_android_text);
        editText.setText(text);

        int textColor = a.getColor(R.styleable.ScrollingLogTextView_android_textColor, Color.BLACK);
        editText.setTextColor(textColor);

        a.recycle();
    }

    public void setText(CharSequence text)
    {
        editText.setText(text);
    }

    public void append(String text)
    {
        editText.append(text);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
