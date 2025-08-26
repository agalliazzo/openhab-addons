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

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link SamsungACProperties} class contains the properties of the Samsung AC.
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SamsungACProperties {
    public static final String[] fanLevels = { "Auto", "Low", "Medium", "High", "Turbo" };
    public static final String[] workModes = { "Auto", "Cool", "Dry", "Wind", "Heat" };
    public static final String[] coModes = { "Off", "Quiet", "Sleep", "Smart", "SoftCool", "TurboMode", "WindMode1",
            "WindMode2", "WindMode3" };
    public static final String[] fanDirection = { "Fixed", "SwingUD" };

    public final Map<String, SamsungACProperty> properties = new HashMap<>();

    SamsungACProperties() {

        properties.put("AC_FUN_ENABLE", new SamsungACProperty());
        properties.put("AC_FUN_POWER", new SamsungACProperty());
        properties.put("AC_FUN_SUPPORTED", new SamsungACProperty());
        properties.put("AC_FUN_OPMODE", new SamsungACProperty());
        properties.put("AC_FUN_TEMPSET", new SamsungACProperty());
        properties.put("AC_FUN_COMODE", new SamsungACProperty());
        properties.put("AC_FUN_ERROR", new SamsungACProperty());
        properties.put("AC_FUN_TEMPNOW", new SamsungACProperty());
        properties.put("AC_FUN_SLEEP", new SamsungACProperty());
        properties.put("AC_FUN_WINDLEVEL", new SamsungACProperty());
        properties.put("AC_FUN_DIRECTION", new SamsungACProperty());
        properties.put("AC_ADD_AUTOCLEAN", new SamsungACProperty());
        properties.put("AC_ADD_APMODE_END", new SamsungACProperty());
        properties.put("AC_ADD_STARTWPS", new SamsungACProperty());
        properties.put("AC_ADD_SPI", new SamsungACProperty());
        properties.put("AC_SG_WIFI", new SamsungACProperty());
        properties.put("AC_SG_INTERNET", new SamsungACProperty());
        properties.put("AC_ADD2_VERSION", new SamsungACProperty());
        properties.put("AC_SG_MACHIGH", new SamsungACProperty());
        properties.put("AC_SG_MACMID", new SamsungACProperty());
        properties.put("AC_SG_MACLOW", new SamsungACProperty());
        properties.put("AC_SG_VENDER01", new SamsungACProperty());
        properties.put("AC_SG_VENDER02", new SamsungACProperty());
        properties.put("AC_SG_VENDER03", new SamsungACProperty());
    }

    public void setValue(String property, Object value) {
        properties.get(property).setValue(value);
    }

    public Object getValue(String property) {
        return properties.get(property).getValue();
    }
}
