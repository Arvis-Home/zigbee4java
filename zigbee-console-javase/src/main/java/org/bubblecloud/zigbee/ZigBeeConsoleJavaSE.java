package org.bubblecloud.zigbee;

import org.bubblecloud.zigbee.network.port.ZigBeeSerialPortImpl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Example runtime arguments on Mac OS-X: /dev/cu.usbmodem1411 4951 22 false
 * @author <a href="mailto:christopherhattonuk@gmail.com">Chris Hatton</a>
 */
public class ZigBeeConsoleJavaSE
{
	private static final int DefaultBaudRate = 115200;

	private ZigBeeConsoleJavaSE(){}

	public static void main(final String[] args){

		final String serialPortName;
		final int channel, pan;
		final boolean resetNetwork;

		try {
			serialPortName = args[0];
			channel        = Integer.parseInt(args[1]);
			pan            = Integer.parseInt(args[2]);
			resetNetwork   = Boolean.parseBoolean(args[3]);
		} catch (final Throwable t) {
            System.out.println("Syntax: java -jar "+getJarName()+" SERIALPORT CHANNEL PAN RESET");
			System.exit(1);
            return;
		}

        final ZigBeeSerialPortImpl serialPort = new ZigBeeSerialPortImpl(serialPortName, DefaultBaudRate);

        final ZigBeeConsole console = new ZigBeeConsole( serialPort, pan, channel, resetNetwork, System.in, System.out );

        console.start();
	}

    private static String getJarName() {

        String jarName;

        try {
            URI uri = ZigBeeConsoleJavaSE.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            jarName = new File(uri).getName();
        }
        catch(URISyntaxException e) {
            jarName = ZigBeeConsoleJavaSE.class.getSimpleName()+".jar";
        }

        return jarName;
    }
}

