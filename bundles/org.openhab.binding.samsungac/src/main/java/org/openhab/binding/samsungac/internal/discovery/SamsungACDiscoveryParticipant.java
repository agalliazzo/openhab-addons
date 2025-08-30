/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.samsungac.internal.discovery;

import static org.openhab.binding.samsungac.internal.SamsungACBindingConstants.THING_TYPE_UID;

import java.util.Set;

import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungACDiscoveryParticipant} is responsible for handling UPnP discovery.
 *
 * @author Alessio Galliazzo - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.samsungac")
public class SamsungACDiscoveryParticipant extends AbstractDiscoveryService {
    private static final int DISCOVERY_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(SamsungACDiscoveryParticipant.class);
    private final SamsungACSSDPListener ssdpListener;
    private final Thread scanningThread;

    public SamsungACDiscoveryParticipant() {
        super(Set.of(THING_TYPE_UID), DISCOVERY_TIMEOUT_SECONDS, true);
        ssdpListener = new SamsungACSSDPListener(packet -> {
            logger.info("Device discovered at {}", packet.get("MAC_ADDR"));
            String host = packet.get("LOCATION");
            String uid = packet.get("MAC_ADDR");

            if (host == null || uid == null)
                return;

            ThingUID thingUID = new ThingUID(THING_TYPE_UID, uid);

            host = host.substring(7);

            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperty("hostname", host)
                    .withThingType(THING_TYPE_UID).withLabel("Samsung AC " + uid).withProperty("UID", uid).build();
            thingDiscovered(result);
        });
        scanningThread = new Thread(ssdpListener);
    }

    // TODO: Implementare il discovery a richiesta
    @Override
    protected void startScan() {
        if (!scanningThread.isAlive())
            scanningThread.start();
        ssdpListener.sendNotify();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
        ssdpListener.stop();
        scanningThread.interrupt();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.info("Starting background discovery");
        if (!scanningThread.isAlive()) {
            logger.info("Starting new scanning thread");
            scanningThread.start();
        }
        logger.info("Sending SSDP notify");
        ssdpListener.sendNotify();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.info("Stopping background discovery");
        ssdpListener.stop();
        try {
            scanningThread.join();
        } catch (InterruptedException ignored) {

        }
        // scanningThread.interrupt();
    }
}
