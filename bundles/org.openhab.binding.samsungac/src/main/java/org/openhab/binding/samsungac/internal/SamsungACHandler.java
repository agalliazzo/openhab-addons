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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    // private @Nullable SamsungACConfiguration config;
    private @NonNull Configuration config = new Configuration();
    private @Nullable SamsungAC samsungac = null;

    public SamsungACHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Probably the SamsungAC protocol can handle the refresh request but I have no idea how, so, for the moment
        // I just ignore it. This doesn't mean that it cannot be implemented.
        if (command instanceof RefreshType) {
            return;
        }

        assert samsungac != null;

        if (CHANNEL_GET_TOKEN.equals(channelUID.getId())) {
            samsungac.getToken();
            // updateState(CHANNEL_GET_TOKEN, OnOffType.OFF);
        }

        if (CHANNEL_POWER.equals(channelUID.getId())) {
            if (command instanceof OnOffType onoff) {
                samsungac.power(onoff == OnOffType.ON);
            }
        }

        if (CHANNEL_SET_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof QuantityType temperature) {
                samsungac.setTemperature(temperature.intValue());
            }
        }

        if (CHANNEL_VIRUS_DOCTOR.equals(channelUID.getId())) {
            if (command instanceof OnOffType onoff) {
                samsungac.virusDoctor(onoff == OnOffType.ON);
            }
        }

        if (CHANNEL_OPMODE.equals(channelUID.getId())) {
            if (command instanceof StringType opmode) {
                samsungac.setWorkMode(opmode.toString());
            }
        }

        if (CHANNEL_COMODE.equals(channelUID.getId())) {
            if (command instanceof StringType opmode) {
                samsungac.setCoolingMode(opmode.toString());
            }
        }

        if (CHANNEL_WIND_LEVEL.equals(channelUID.getId())) {
            if (command instanceof StringType windLevel) {
                samsungac.setWindLevel(windLevel.toString());
            }
        }

        if (CHANNEL_VANE_VERTICAL.equals(channelUID.getId())) {
            if (command instanceof StringType vane) {
                samsungac.setFanDirection(vane.toString());
            }
        }
    }

    @org.eclipse.jdt.annotation.NonNullByDefault({})
    class PropertyChangedCallback implements SamsungACPropertyChangedCallback {
        @Override
        public void onPropertyChanged(@NonNull String property, @NonNull Object newValue) {
            switch (property) {
                case "GetToken":
                    updateState(CHANNEL_GET_TOKEN, OnOffType.OFF);
                    assert newValue instanceof String;
                    assert config != null;
                    config.put("token", newValue);

                    // updateStatus(ThingStatus.OFFLINE);
                    assert samsungac != null;
                    dispose();
                    updateConfiguration(config);
                    initialize();
                    // initialize();
                    break;
                case "AC_FUN_POWER":
                    assert newValue instanceof Boolean;
                    updateState(CHANNEL_POWER, OnOffType.from((Boolean) newValue));
                    break;
                case "AC_ADD_SPI":
                    assert newValue instanceof Boolean;
                    updateState(CHANNEL_VIRUS_DOCTOR, OnOffType.from((Boolean) newValue));
                    break;
                case "AC_ADD_AUTOCLEAN":
                    assert newValue instanceof Boolean;
                    updateState(CHANNEL_AUTOCLEAN, OnOffType.from((Boolean) newValue));
                    break;
                case "AC_SG_INTERNET":
                    assert newValue instanceof Boolean;
                    updateState(CHANNEL_INTERNET_CONNECTED, OnOffType.from((Boolean) newValue));
                    break;
                case "AC_FUN_OPMODE":
                    assert newValue instanceof String;
                    updateState(CHANNEL_OPMODE, new StringType((String) newValue));
                    break;
                case "AC_FUN_TEMPSET":
                    assert newValue instanceof Integer;
                    updateState(CHANNEL_SET_TEMPERATURE, new DecimalType((Integer) newValue));
                    break;
                case "AC_FUN_COMODE":
                    assert newValue instanceof String;
                    updateState(CHANNEL_COMODE, new StringType((String) newValue));
                    break;
                case "AC_FUN_WINDLEVEL":
                    assert newValue instanceof String;
                    updateState(CHANNEL_WIND_LEVEL, new StringType((String) newValue));
                    break;
                case "AC_FUN_DIRECTION":
                    assert newValue instanceof String;
                    updateState(CHANNEL_VANE_VERTICAL, new StringType((String) newValue));
                    break;
                case "AC_FUN_TEMPNOW":
                    assert newValue instanceof Integer;
                    updateState(CHANNEL_MEASURED_TEMPERATURE, new DecimalType((Integer) newValue));
                    break;
                case "AC_FUN_ERROR":
                case "AC_FUN_ENABLE":
                case "AC_FUN_SLEEP":
                case "AC_ADD_APMODE_END":
                case "AC_ADD_STARTWPS":
                case "AC_SG_WIFI":
                case "AC_ADD2_VERSION":
                case "AC_SG_MACHIGH":
                case "AC_SG_MACMID":
                case "AC_SG_MACLOW":
                case "AC_SG_VENDER01":
                case "AC_SG_VENDER02":
                case "AC_SG_VENDER03":
                case "AC_FUN_SUPPORTED":
                default:
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        updateStatus(ThingStatus.OFFLINE);
        if (samsungac != null) {
            samsungac.disconnect();
        }
        samsungac = null;
    }

    @Override
    public void initialize() {
        // config = getConfigAs(SamsungACConfiguration.class);
        config = getConfig();

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {

            try {
                String token = (String) config.get("token");
                samsungac = new SamsungAC((String) config.get("hostname"), (String) config.get("UID"), token);
                samsungac.connect();
                samsungac.setCallback(new PropertyChangedCallback());
                if (token == null || token.isEmpty()) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Please, read the documentation and set the token");
                    return;
                }
                samsungac.authenticate();

            } catch (UnknownHostException | SamsungACAuthenticationFailedException
                    | SamsungACConnectionFailedException e) {
                // 3 Exceptions can be thrown here:
                // UnknownHostException: if the hostname cannot be resolved
                // SamsungACAuthenticationFailedException: if the authentication fails
                // SamsungACConnectionFailedException: if the connection fails
                // In any case, the initialization fails and the status is set to OFFLINE
                logger.error("Error initializing SamsungAC", e);
                updateStatus(ThingStatus.OFFLINE);
                return;
            }

            // If everything went well, the initialization was successful and the status is set to ONLINE
            updateStatus(ThingStatus.ONLINE);
        });
    }
}
