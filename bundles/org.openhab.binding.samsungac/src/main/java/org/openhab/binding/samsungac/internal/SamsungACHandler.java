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
package org.openhab.binding.samsungac.internal;

import static org.openhab.binding.samsungac.internal.SamsungACBindingConstants.*;

import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungACHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alessio Galliazzo - Initial contribution
 */
@NonNullByDefault
public class SamsungACHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SamsungACHandler.class);

    private @Nullable SamsungACConfiguration config;
    boolean thingReachable = true;
    @Nullable
    private SamsungAC samsungac = null;

    public SamsungACHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        assert samsungac != null;
        if (CHANNEL_POWER.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {

            } else if (command instanceof OnOffType onoff) {
                samsungac.power(onoff == OnOffType.ON);
            }
        }

        if (CHANNEL_SET_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {

            } else if (command instanceof QuantityType temperature) {
                samsungac.set_temperature(temperature.intValue());
            }
        }

        if (CHANNEL_VIRUS_DOCTOR.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {

            } else if (command instanceof OnOffType onoff) {
                samsungac.virus_doctor(onoff == OnOffType.ON);
            }
        }

        // if (CHANNEL_1.equals(channelUID.getId())) {
        // if (command instanceof RefreshType) {
        // // TODO: handle data refresh
        // }
        //
        // // TODO: handle command
        //
        // // Note: if communication with thing fails for some reason,
        // // indicate that by setting the status with detail information:
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // // "Could not control device at IP address x.x.x.x");
        // }
        // if()
    }

    void parseIncomingData() {
    }

    @Override
    public void initialize() {
        config = getConfigAs(SamsungACConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            // <background task with long-running initialization here>
            try {
                samsungac = new SamsungAC(config.hostname, config.UID, config.token);
            } catch (UnknownHostException e) {
                thingReachable = false;
                updateStatus(ThingStatus.OFFLINE);
                logger.error("Error connecting to Samsung AC", e);
                return;
            }
            // SamsungAC ac = new SamsungAC("192.168.2.241", "7825AD1163E3",
            // "16968012-2892-M993-N707-373832354144");
            // SamsungAC ac = new SamsungAC("192.168.2.241", "7825AD1163E3",
            // "16968012-2892-M993-N707-373832354144");
            // SamsungAC ac = new SamsungAC("192.168.2.241", "7825AD1163E3",
            // "16968012-2892-M993-N707-373832354144");
            // samsungac = new SamsungAC(config.hostname, config.UID, config.token);
            // Thread.sleep(1000);
            samsungac.connect();
            // Thread.sleep(1000);
            samsungac.authenticate();
            thingReachable = true;

            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
