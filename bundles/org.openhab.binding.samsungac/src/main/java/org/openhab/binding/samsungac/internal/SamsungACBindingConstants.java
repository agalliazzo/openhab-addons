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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SamsungACBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alessio Galliazzo - Initial contribution
 */
@NonNullByDefault
public class SamsungACBindingConstants {

    private static final String BINDING_ID = "samsungac";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "Airconditioner");

    // List of all Channel ids

    public static final String CHANNEL_POWER = "powerChannel";
    public static final String CHANNEL_OPMODE = "operationMode";
    public static final String CHANNEL_MEASURED_TEMPERATURE = "measuredTemperature";
    public static final String CHANNEL_SET_TEMPERATURE = "setTemperature";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_VANE_VERTICAL = "vaneVertical";
    public static final String CHANNEL_VIRUS_DOCTOR = "virusDoctor";
    public static final String CHANNEL_COMODE = "coMode";
}
