package org.bubblecloud.zigbee;

import org.bubblecloud.zigbee.ZigBeeApi.Status;
import org.bubblecloud.zigbee.api.Device;
import org.bubblecloud.zigbee.api.cluster.general.OnOff;
import org.bubblecloud.zigbee.network.discovery.ZigBeeDiscoveryManager;
import org.bubblecloud.zigbee.network.model.DiscoveryMode;
import org.bubblecloud.zigbee.network.model.DriverStatus;
import org.bubblecloud.zigbee.network.model.NetworkMode;
import org.bubblecloud.zigbee.network.port.ZigBeeNetworkManagerImpl;
import org.bubblecloud.zigbee.network.port.ZigBeePort;
import org.bubblecloud.zigbee.util.lifecycle.LifecycleState;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.bubblecloud.zigbee.util.concurrent.ZigBeeExecutor.zigBeeExecutor;


/**
 * Test for ZigBeeNetworkManagerSerialImpl.
 */
public abstract class ZigBeeNetworkTest {
    private final static Logger logger = LoggerFactory.getLogger(ZigBeeNetworkTest.class);
	private final ZigBeePort port;

	protected ZigBeeNetworkTest(ZigBeePort port) {
		this.port = port;
	}

    @Before
    public void setup() {
    }

	@Test
    @Ignore
    public void testOpenNetwork() throws Exception {

        final ZigBeeNetworkManagerImpl zigbeeNetwork = new ZigBeeNetworkManagerImpl(
                port, NetworkMode.Coordinator, 4951, 22,
                false, 2500L);
        zigbeeNetwork.startup();

        zigbeeNetwork.getDriverStatus().waitFor(DriverStatus.NETWORK_READY);

        zigbeeNetwork.shutdown();
    }

	@Test
    @Ignore
    public void testDiscoverNetwork() throws Exception {

        final ZigBeeNetworkManagerImpl zigbeeNetwork = new ZigBeeNetworkManagerImpl(
                port, NetworkMode.Coordinator, 4951, 22,
                false, 2500L);

        final ZigBeeDiscoveryManager zigbeeDiscoveryManager = new ZigBeeDiscoveryManager(zigbeeNetwork, DiscoveryMode.ALL);

        zigbeeDiscoveryManager.startup();
        zigbeeNetwork.startup();

        zigbeeNetwork.getDriverStatus().waitFor(DriverStatus.NETWORK_READY);

        final Runnable shutdownTask = new Runnable()
        {
            @Override
            public void run()
            {
                zigbeeDiscoveryManager.shutdown();
                zigbeeNetwork.shutdown();
            }
        };

        zigBeeExecutor.schedule(shutdownTask, 20, TimeUnit.SECONDS);
    }

    @Test
    @Ignore
    public void testZigBeeApi() throws Exception {
        
        final EnumSet<DiscoveryMode> discoveryModes = DiscoveryMode.ALL;
        discoveryModes.remove(DiscoveryMode.LinkQuality);
        final ZigBeeApi zigbeeApi = new ZigBeeApi(port, 4951, 11, false, discoveryModes);

        zigbeeApi.startup();

        LifecycleState state = zigbeeApi.getState().waitForAnyOf(LifecycleState.Started, LifecycleState.Error);

        switch( state ) {
            case Started: {

                logger.info("Listing devices:");
                for (final Device device : zigbeeApi.getZigBeeApiContext().getDevices()) {
                    logger.info(device.getClass().getSimpleName() + " : " + device.getEndpoint().getEndpointId());
                }

                while (true) {
                    try {
                        final Device
                            switchDevice = zigbeeApi.getZigBeeApiContext().getDevice("00:12:4B:00:01:DD:8B:21/2" ),
                            lampDevice   = zigbeeApi.getZigBeeApiContext().getDevice("00:17:88:01:00:BE:51:EC/11");

                        if (lampDevice == null) {
                            continue;
                        }

                        Thread.sleep(1000);

                        final OnOff onOff = lampDevice.getCluster(OnOff.class);
                        onOff.off();

                        break;
                    } catch (final Throwable t) {
                        logger.error("Error getting information for device.", t);
                        break;
                    }
                }

                zigbeeApi.shutdown();
            }
            break;

            case Error: {
                logger.error("Error: Couldn't start Zigbee API.");
            }
            break;
        }
    }
}
