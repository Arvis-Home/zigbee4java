/*
   Copyright 2008-2013 CNR-ISTI, http://isti.cnr.it
   Institute of Information Science and Technologies
   of the Italian National Research Council


   See the NOTICE file distributed with this work for additional
   information regarding copyright ownership

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.bubblecloud.zigbee.network.discovery;

import org.bubblecloud.zigbee.api.cluster.impl.api.core.Status;
import org.bubblecloud.zigbee.lang.observe.Observable;
import org.bubblecloud.zigbee.network.ApplicationFrameworkMessageListener;
import org.bubblecloud.zigbee.network.ZigBeeNetworkManager;
import org.bubblecloud.zigbee.network.impl.ApplicationFrameworkLayer;
import org.bubblecloud.zigbee.network.impl.ZigBeeNetwork;
import org.bubblecloud.zigbee.network.impl.ZigBeeNodeImpl;
import org.bubblecloud.zigbee.network.model.DiscoveryMode;
import org.bubblecloud.zigbee.network.packet.ZToolAddress16;
import org.bubblecloud.zigbee.network.packet.af.AF_INCOMING_MSG;
import org.bubblecloud.zigbee.network.packet.zdo.ZDO_IEEE_ADDR_REQ;
import org.bubblecloud.zigbee.network.packet.zdo.ZDO_IEEE_ADDR_RSP;
import org.bubblecloud.zigbee.util.Integers;
import org.bubblecloud.zigbee.util.concurrent.ZigBeeExecutor;
import org.bubblecloud.zigbee.util.lifecycle.AbstractLifecycleObject;
import org.bubblecloud.zigbee.util.lifecycle.LifecycleState;
import org.bubblecloud.zigbee.lang.observe.ObservableState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;


/**
 * This class is tracks the {@link org.bubblecloud.zigbee.network.ZigBeeNetworkManager} service available <br>
 * and it creates all the resources required by this implementation of the <i>ZigBee Base Driver</i>
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:tommi.s.e.laukkanen@gmail.com">Tommi S.E. Laukkanen</a>
 */
public class ZigBeeDiscoveryManager extends AbstractLifecycleObject<ZigBeeDiscoveryManagerObserver> implements ApplicationFrameworkMessageListener {

    private final ObservableState<LifecycleState> state = new ObservableState<>(LifecycleState.Stopped);

    private final static Logger logger = LoggerFactory.getLogger(ZigBeeDiscoveryManager.class);

    private final ZigBeeNetworkManager   networkManager;
    private final ImportingQueue         importingQueue;
    private final EnumSet<DiscoveryMode> enabledDiscoveries;
    private final Set<Integer>           inspectedNetworkAddresses = new HashSet<>();

    private AnnounceListenerImpl                announceListener;
    private AssociationNetworkBrowser           associationNetworkBrowser;
    private LinkQualityIndicatorNetworkBrowser  linkQualityIndicatorNetworkBrowser;
    private EndpointBuilder                     endpointBuilder;

    public ZigBeeDiscoveryManager(ZigBeeNetworkManager networkManager, final EnumSet<DiscoveryMode> enabledDiscoveries) {
        super(ZigBeeDiscoveryManagerObserver.class);
        importingQueue          = new ImportingQueue();
        this.networkManager     = networkManager;
        this.enabledDiscoveries = enabledDiscoveries;
    }

    public void startup() {

        state.set(LifecycleState.Starting);

        logger.trace("Setting up all the importer data and threads");
        importingQueue.clear();
        ApplicationFrameworkLayer.getAFLayer(networkManager);

        if (enabledDiscoveries.contains(DiscoveryMode.Announce)) {
            announceListener = new AnnounceListenerImpl(importingQueue, networkManager);
            networkManager.addAnnunceListener(announceListener);
        } else {
            logger.trace("ANNOUNCE discovery disabled.");
        }

        if (enabledDiscoveries.contains(DiscoveryMode.Addressing)) {
            associationNetworkBrowser = new AssociationNetworkBrowser(importingQueue, networkManager, ZigBeeExecutor.zigBeeExecutor);
            associationNetworkBrowser.start();
        } else {
            logger.trace("{} discovery disabled.", AssociationNetworkBrowser.class);
        }

        if (enabledDiscoveries.contains(DiscoveryMode.LinkQuality)) {
            linkQualityIndicatorNetworkBrowser = new LinkQualityIndicatorNetworkBrowser(importingQueue, networkManager, ZigBeeExecutor.zigBeeExecutor);
            linkQualityIndicatorNetworkBrowser.start();
        } else {
            logger.trace("{} discovery disabled.", LinkQualityIndicatorNetworkBrowser.class);
        }

        endpointBuilder = new EndpointBuilder(importingQueue, networkManager);
        new Thread(endpointBuilder, "EndpointBuilder[" + networkManager + "]").start();

        networkManager.addAFMessageListener(this);

        state.set(LifecycleState.Started);
    }

    public void shutdown() {

        state.set(LifecycleState.Stopping);

        //logger.info("Driver used left:clean up all the data and close all the threads");

        networkManager.removeAnnounceListener(announceListener);

        if (associationNetworkBrowser != null) {
            associationNetworkBrowser.stop();
        }

        if (linkQualityIndicatorNetworkBrowser != null) {
            linkQualityIndicatorNetworkBrowser.stop();
        }

        if (endpointBuilder != null) {
            endpointBuilder.end();
        }

        importingQueue.close();

        state.set(LifecycleState.Stopped);
    }

    private boolean isInitialNetworkBrowsingComplete = false;
    private boolean isInitialNetworkBrowsingComplete() { return isInitialNetworkBrowsingComplete; }

    private boolean refreshInitialNetworkBrowsingComplete() {
        return (associationNetworkBrowser == null || associationNetworkBrowser.isInitialNetworkBrowsingComplete())
                && endpointBuilder.getState();


    }

    @Override
    public void notify(AF_INCOMING_MSG msg) {
        final int sourceNetworkAddress = msg.getSrcAddr();

        synchronized (inspectedNetworkAddresses) {
            if (!inspectedNetworkAddresses.contains(sourceNetworkAddress)) {
                inspectedNetworkAddresses.add(sourceNetworkAddress);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        inspectNetworkAddress(sourceNetworkAddress);
                    }
                }).start();
            }
        }
    }

    /**
     * Inspect given network address.
     *
     * @param sourceNetworkAddress the network address to inspect
     */
    private synchronized void inspectNetworkAddress(final int sourceNetworkAddress) {
        logger.debug("Inspecting node based on incoming AF message from network address #{}.",
                sourceNetworkAddress);

        final ZDO_IEEE_ADDR_RSP result = networkManager.sendZDOIEEEAddressRequest(
                new ZDO_IEEE_ADDR_REQ(sourceNetworkAddress, ZDO_IEEE_ADDR_REQ.REQ_TYPE.SINGLE_DEVICE_RESPONSE, (byte) 0)
        );

        if (result == null) {
            logger.debug("Node did not respond to ZDO_IEEE_ADDR_REQ #{}", sourceNetworkAddress);
        } else if (result.Status == 0) {
            logger.debug("Node network address #{} resolved to IEEE address {}.", sourceNetworkAddress, result.getIeeeAddress());
            final ZigBeeNodeImpl node = new ZigBeeNodeImpl(sourceNetworkAddress, result.getIeeeAddress(),
                    (short) networkManager.getCurrentPanId());

            ZToolAddress16 nwk = new ZToolAddress16(
                    Integers.getByteAsInteger(sourceNetworkAddress, 1),
                    Integers.getByteAsInteger(sourceNetworkAddress, 0)
            );
            importingQueue.push(nwk, result.getIeeeAddress());

            final ZigBeeNetwork network = ApplicationFrameworkLayer.getAFLayer(networkManager).getZigBeeNetwork();
            network.notifyNodeBrowsed(node);
        } else {
            logger.warn("Node #{} ZDO_IEEE_ADDR_REQ failed with state {} ", sourceNetworkAddress,
                    Status.getStatus((byte) result.Status));
        }
    }

    @Override
    protected void startImpl()
    {

    }

    @Override
    protected void stopImpl()
    {

    }
}
