package org.bubblecloud.zigbee.console;

import org.apache.commons.io.FileUtils;
import org.bubblecloud.zigbee.ZigBeeApi;
import org.bubblecloud.zigbee.api.Device;
import org.bubblecloud.zigbee.api.DeviceListener;
import org.bubblecloud.zigbee.console.command.ConsoleCommand;
import org.bubblecloud.zigbee.console.command.impl.BindCommand;
import org.bubblecloud.zigbee.console.command.impl.ColorCommand;
import org.bubblecloud.zigbee.console.command.impl.DescribeCommand;
import org.bubblecloud.zigbee.console.command.impl.HelpCommand;
import org.bubblecloud.zigbee.console.command.impl.JoinCommand;
import org.bubblecloud.zigbee.console.command.impl.LevelCommand;
import org.bubblecloud.zigbee.console.command.impl.ListCommand;
import org.bubblecloud.zigbee.console.command.impl.ListenCommand;
import org.bubblecloud.zigbee.console.command.impl.OffCommand;
import org.bubblecloud.zigbee.console.command.impl.OnCommand;
import org.bubblecloud.zigbee.console.command.impl.QuitCommand;
import org.bubblecloud.zigbee.console.command.impl.ReadCommand;
import org.bubblecloud.zigbee.console.command.impl.SubscribeCommand;
import org.bubblecloud.zigbee.console.command.impl.UnbindCommand;
import org.bubblecloud.zigbee.console.command.impl.UnlistenCommand;
import org.bubblecloud.zigbee.console.command.impl.UnsubscribeCommand;
import org.bubblecloud.zigbee.console.command.impl.WriteCommand;
import org.bubblecloud.zigbee.lang.observe.StateChangeObserver;
import org.bubblecloud.zigbee.network.model.DiscoveryMode;
import org.bubblecloud.zigbee.network.port.ZigBeePort;
import org.bubblecloud.zigbee.util.lifecycle.LifecycleState;
import org.bubblecloud.zigbee.lang.observe.ObservableState;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * ZigBee command line console is an example usage of ZigBee API.
 * It requires a ZigBeePort implementation to function.
 *
 * For a ready-to-run demonstration on a Desktop PC equipped with CC2531 dongle:
 * - Check-out the 'zigbee4java-serialPort' module
 * - Execute class 'ZigBeeSerialConsole' with appropriate params
 *
 * @author <a href="mailto:tommi.s.e.laukkanen@gmail.com">Tommi S.E. Laukkanen</a>
 * @author <a href="mailto:christopherhattonuk@gmail.com">Chris Hatton</a>
 */
public final class ZigBeeConsole {

    private ObservableState<LifecycleState> state = new ObservableState<>(LifecycleState.Stopped);
    public final ObservableState<LifecycleState> getState() { return state; }

    private static final String NetworkStateFileName = "network.json";
    private static final File   NetworkStateFile     = new File(NetworkStateFileName);

    private BufferedReader bufferedReader = null;

    private final Runnable shutdownHookRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if(state.is(LifecycleState.Started))
            {
                state.set(LifecycleState.Stopping);
            }

            try
            {
                inputStream.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    };

    /**
     * Map of registered commands and their implementations.
     */
    private final Map<String, ConsoleCommand> commands = new HashMap<>();

	private final ZigBeePort port;
	private final int pan, channel;
	private final boolean resetNetwork;

    private final InputStream inputStream;
    private final PrintStream printStream;

	public ZigBeeConsole(ZigBeePort port, int pan, int channel, boolean resetNetwork, InputStream inputStream, OutputStream outputStream) {
		this.port           = port;
		this.pan            = pan;
		this.channel        = channel;
		this.resetNetwork   = resetNetwork;
        this.inputStream    = inputStream;
        this.printStream    = new PrintStream(outputStream);
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        initCommandList();
	}

    private void initCommandList() {
        ConsoleCommand[] buildCommands = new ConsoleCommand[] {
                new QuitCommand         (),
                new HelpCommand         (),
                new ListCommand         (),
                new DescribeCommand     (),
                new BindCommand         (),
                new UnbindCommand       (),
                new OnCommand           (),
                new OffCommand          (),
                new ColorCommand        (),
                new LevelCommand        (),
                new ListenCommand       (),
                new UnlistenCommand     (),
                new SubscribeCommand    (),
                new UnsubscribeCommand  (),
                new ReadCommand         (),
                new WriteCommand        (),
                new JoinCommand         ()
        };

        for(ConsoleCommand buildCommand : buildCommands) {
            commands.put(buildCommand.getName(), buildCommand);
        }
    }

    public Collection<ConsoleCommand> getCommands() {
        return commands.values();
    }

    private ZigBeeApi zigBeeApi;

    public final ZigBeeApi getZigBeeApi() {
        return zigBeeApi;
    }

	/**
     * Starts this console.  This method returns immediately while the console startup process
     * continues on it's own Thread.  To monitor when the console has completed startup, add
     * an observer to this Console's ObservableStatus, via getStatus().addObserver(...) .
     */
    public void start() {

        state.set(LifecycleState.Starting);

        printStream.print("ZigBee Console starting up...");

        zigBeeApi = new ZigBeeApi(port, pan, channel, resetNetwork, getDiscoveryModes());
        zigBeeApi.getState().addObserver(zigbeeApiStateChangeObserver);

        restoreNetworkState(zigBeeApi);

        zigBeeApi.startup();
    }

    /**
     * Stops this console.  This method returns immediately while the console shutdown process
     * continues on it's own Thread.  To monitor when the console has completed shutdown, add
     * an observer to this Console's ObservableStatus, via getStatus().addObserver(...) .
     */
    public void stop() {

        state.set(LifecycleState.Stopping);

        printStream.print("ZigBee Console shutting down...");

        zigBeeApi.shutdown();
    }

    /**
     * The Discovery modes returned here will be use as a construction parameter for ZigBeeApi
     * during Console startup.
     */
    private EnumSet<DiscoveryMode> getDiscoveryModes()
    {
        final EnumSet<DiscoveryMode> discoveryModes = DiscoveryMode.ALL;
        //discoveryModes.remove(DiscoveryMode.LinkQuality);
        return discoveryModes;
    }

    /**
     * Called during Console startup.
     */
    private static void restoreNetworkState(ZigBeeApi zigBeeApi) {
        if (NetworkStateFile.exists()) {
            try {
                final String networkState = FileUtils.readFileToString(NetworkStateFile);
                zigBeeApi.deserializeNetworkState(networkState);
            } catch (final Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * Called during Console shutdown.
     */
    private static void saveNetworkState(ZigBeeApi zigBeeApi) {
        try {
            FileUtils.writeStringToFile(NetworkStateFile, zigBeeApi.serializeNetworkState(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final StateChangeObserver<LifecycleState> zigbeeApiStateChangeObserver = new StateChangeObserver<LifecycleState>() {
        @Override
        public void onStateChanged(final LifecycleState newStatus)
        {
            switch(newStatus)
            {
                case Started:
                    print("ZigBee API starting up ... [OK]");

                    zigBeeApi.addDeviceListener(loggingDeviceListener);

                    consoleThread.start();
                    break;

                case Stopped:

                    print("ZigBee API stopped normally");

                    zigBeeApi.removeDeviceListener(loggingDeviceListener);

                    saveNetworkState(zigBeeApi);

                    zigBeeApi = null;

                    state.set(LifecycleState.Stopped);

                    break;

                case Error:
                    print("ZigBee API error");
                    break;
            }
        }
    };

    private final DeviceListener loggingDeviceListener = new DeviceListener()
    {
        @Override
        public void deviceAdded(Device device)
        {
            print("Device added: " + device.getEndpointId() + " (#" + device.getNetworkAddress() + ")");
        }

        @Override
        public void deviceUpdated(Device device)
        {
            print("Device updated: " + device.getEndpointId() + " (#" + device.getNetworkAddress() + ")");
        }

        @Override
        public void deviceRemoved(Device device)
        {
            print("Device removed: " + device.getEndpointId() + " (#" + device.getNetworkAddress() + ")");
        }
    };

    private Runnable consoleThreadMain = new Runnable()
    {
        @Override
        public void run()
        {
            // TODO Use something like a command line parameter to decide if permit join is re-enabled
            // Lets disable the join functionality in console by default to improve security.
        /*if (!zigBeeApi.permitJoin(true)) {
            print("ZigBee API permit join enable ... [FAIL]");
        } else {
            print("ZigBee API permit join enable ... [OK]");
        }*/


            printStream.print("Browsing network for the first time...");
            while (!(state.is(LifecycleState.Stopping)) && !NetworkStateFile.exists() && !zigBeeApi.isInitialBrowsingComplete()) {

                printStream.print('.');
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    break;
                }
            }
            printStream.println("[OK]");

            print("There are " + zigBeeApi.getDevices().size() + " known devices in the network.");

            print("ZigBee console ready.");

            String inputLine;
            while (!(state.is(LifecycleState.Stopping)) && (inputLine = readLine()) != null) {processInputLine(inputLine);
            }

            stop();
        }
    };

    private final Thread consoleThread = new Thread(consoleThreadMain, "ZigBee Console Main Thread");

    public ConsoleCommand getCommandByName(String name) {
        return commands.get(name);
    }

    /**
     * Processes text input line.
     * This ZigBeeConsole must be in started state before calling this method.
     * Calling this when not in started state will cause a RuntimeException to be thrown.
     * @param inputLine the input line
     */
    public void processInputLine(final String inputLine) {
        if(zigBeeApi ==null) {
            throw new RuntimeException("Attempted to process input line before this console was started.");
        }

        if (inputLine.length() == 0) {
            return;
        }
        final String[] args = inputLine.split(" ");
        try {
            if (commands.containsKey(args[0])) {
                executeCommand(args[0], args);
                return;
            } else {
                for (final String command : commands.keySet()) {
                    if (command.charAt(0) == inputLine.charAt(0)) {
                        executeCommand(command, args);
                        return;
                    }
                }
                print("Uknown command. Use 'help' command to list available commands.");
            }
        } catch (final Exception e) {
            print("Exception in command execution: ");
            e.printStackTrace();
        }
    }

    /**
     * Executes command.
     * @param command the command
     * @param args the arguments including the command
     */
    private void executeCommand(final String command, final String[] args) {
        final ConsoleCommand consoleCommand = commands.get(command);
        if (!consoleCommand.process(this, args)) {
            print(consoleCommand.getSyntax());
        }
    }

    /**
     * Prints line to console.
     *
     * @param line the line
     */
    public void print(final String line) {
        printStream.println(System.lineSeparator() + line);
    }

    /**
     * Reads line from console.
     *
     * @return line readLine from console or null if exception occurred.
     */
    private String readLine() {
        String inputLine = "";
        printStream.print(System.lineSeparator()+"> ");
        try {

            inputLine = bufferedReader.readLine();

            //int c;
            //while((c=inputStream.read())!='\n')
            //{
            //    inputLine += (char)c;
            //}

            //inputLine = bufferRead.readLine();
        } catch(final IOException e) {
            inputLine = null;
        }

        return inputLine;
    }

    /**
     * Gets device from ZigBee API either with index or endpoint ID
     * @param zigbeeApi the zigbee API
     * @param deviceIdentifier the device identifier
     * @return
     */
    public Device getDeviceByIndexOrEndpointId(ZigBeeApi zigbeeApi, String deviceIdentifier) {
        Device device;
        try {
            device = zigbeeApi.getDevices().get(Integer.parseInt(deviceIdentifier));
        } catch (final Exception e) {
            device = zigbeeApi.getDevice(deviceIdentifier);
        }
        return device;
    }
}
