package org.bubblecloud.zigbee.androidconsole;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import org.bubblecloud.zigbee.ZigBeeConsole;
import org.bubblecloud.zigbee.androidconsole.io.EditTextInputStream;
import org.bubblecloud.zigbee.androidconsole.io.ScrollingLogTextViewOutputStream;
import org.bubblecloud.zigbee.androidconsole.ui.ScrollingLogTextView;
import org.bubblecloud.zigbee.network.port.AndroidUsbSerialPort;


public class ZigbeeConsoleActivity extends Activity
{
    private static final int
            Zigbee_Channel = 11,
            Zigbee_PAN_ID  = 4952;

    private EditText             editText;
    private ScrollingLogTextView logText;

    private EditTextInputStream              inputStream;
    private ScrollingLogTextViewOutputStream outputStream;
    private ZigBeeConsole console;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_zigbee_console);

        editText = (EditText)            (findViewById(R.id.consoleInputEditText));
        logText  = (ScrollingLogTextView)(findViewById(R.id.consoleOutputTextView));

        logText.setText("");

        inputStream  = new EditTextInputStream(editText);
        outputStream = new ScrollingLogTextViewOutputStream(logText);

        final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        final AndroidUsbSerialPort usbSerialPort = new AndroidUsbSerialPort(usbManager, this);

        console = new ZigBeeConsole(usbSerialPort, Zigbee_PAN_ID, Zigbee_Channel, false, inputStream, outputStream);

        console.start();
    }
}
