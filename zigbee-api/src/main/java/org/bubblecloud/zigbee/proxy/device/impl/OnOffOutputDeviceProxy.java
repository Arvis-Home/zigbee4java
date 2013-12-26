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

package org.bubblecloud.zigbee.proxy.device.impl;

import org.bubblecloud.zigbee.ZigbeeConstants;
import org.bubblecloud.zigbee.ZigbeeProxyContext;
import org.bubblecloud.zigbee.network.ZigBeeDevice;
import org.bubblecloud.zigbee.proxy.*;
import org.bubblecloud.zigbee.proxy.cluster.general.Groups;
import org.bubblecloud.zigbee.proxy.cluster.general.OnOff;
import org.bubblecloud.zigbee.proxy.cluster.general.Scenes;
import org.bubblecloud.zigbee.proxy.device.generic.OnOffOutput;
import org.bubblecloud.zigbee.proxy.device.lighting.OnOffLight;

/**
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @version $LastChangedRevision: 799 $ ($LastChangedDate: 2013-08-06 19:00:05 +0300 (Tue, 06 Aug 2013) $)
 * @since 0.2.0
 */
public class OnOffOutputDeviceProxy extends DeviceProxyBase implements OnOffOutput {

    private OnOff onOff;
    private Scenes scenes;
    private Groups groups;

    public OnOffOutputDeviceProxy(ZigbeeProxyContext ctx, ZigBeeDevice zbDevice) throws ZigBeeHAException {
        super(ctx, zbDevice);
        onOff = (OnOff) getCluster(ZigbeeConstants.CLUSTER_ID_ON_OFF);
        groups = (Groups) getCluster(ZigbeeConstants.CLUSTER_ID_GROUPS);
        scenes = (Scenes) getCluster(ZigbeeConstants.CLUSTER_ID_SCENES);
    }


    public Groups getGroups() {
        return groups;
    }

    public OnOff getOnOff() {
        return onOff;
    }

    public Scenes getScenes() {
        return scenes;
    }


    @Override
    public String getName() {
        return OnOffOutput.NAME;
    }

    public final static DeviceDescription DEVICE_DESCRIPTOR = new AbstractDeviceDescription() {

        public int[] getCustomClusters() {
            return OnOffLight.CUSTOM;
        }

        public int[] getMandatoryCluster() {
            return OnOffLight.MANDATORY;
        }

        public int[] getOptionalCluster() {
            return OnOffLight.OPTIONAL;
        }

        public int[] getStandardClusters() {
            return OnOffLight.STANDARD;
        }

    };

    @Override
    public DeviceDescription getDescription() {
        return DEVICE_DESCRIPTOR;
    }


}
