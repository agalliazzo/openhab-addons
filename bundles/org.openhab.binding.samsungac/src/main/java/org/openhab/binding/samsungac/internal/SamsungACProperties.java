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

/**
 * The {@link SamsungACProperties} class contains the properties of the Samsung AC.
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SamsungACProperties extends HashMap<String, SamsungACProperty> {
    public static final String[] fanLevels = { "Auto", "Low", "Medium", "High", "Turbo" };
    public static final String[] workModes = { "Auto", "Cool", "Dry", "Wind", "Heat" };
    public static final String[] coModes = { "Off", "Quiet", "Sleep", "Smart", "SoftCool", "TurboMode", "WindMode1",
            "WindMode2", "WindMode3" };
    public static final String[] fanDirection = { "Fixed", "SwingUD" };

    SamsungACProperties() {
        put("AC_FUN_ENABLE", new SamsungACProperty());
        put("AC_FUN_POWER", new SamsungACProperty());
        put("AC_FUN_SUPPORTED", new SamsungACProperty());
        put("AC_FUN_OPMODE", new SamsungACProperty());
        put("AC_FUN_TEMPSET", new SamsungACProperty());
        put("AC_FUN_COMODE", new SamsungACProperty());
        put("AC_FUN_ERROR", new SamsungACProperty());
        put("AC_FUN_TEMPNOW", new SamsungACProperty());
        put("AC_FUN_SLEEP", new SamsungACProperty());
        put("AC_FUN_WINDLEVEL", new SamsungACProperty());
        put("AC_FUN_DIRECTION", new SamsungACProperty());
        put("AC_ADD_AUTOCLEAN", new SamsungACProperty());
        put("AC_ADD_APMODE_END", new SamsungACProperty());
        put("AC_ADD_STARTWPS", new SamsungACProperty());
        put("AC_ADD_SPI", new SamsungACProperty());
        put("AC_SG_WIFI", new SamsungACProperty());
        put("AC_SG_INTERNET", new SamsungACProperty());
        put("AC_ADD2_VERSION", new SamsungACProperty());
        put("AC_SG_MACHIGH", new SamsungACProperty());
        put("AC_SG_MACMID", new SamsungACProperty());
        put("AC_SG_MACLOW", new SamsungACProperty());
        put("AC_SG_VENDER01", new SamsungACProperty());
        put("AC_SG_VENDER02", new SamsungACProperty());
        put("AC_SG_VENDER03", new SamsungACProperty());
        put("GetToken", new SamsungACProperty());
        put("AccountValidated", new SamsungACProperty(false));
    }

    public void setValue(String property, Object value) {
        get(property).setValue(value);
    }

    public Object getValue(String property) {
        SamsungACProperty prop = get(property);
        if (prop == null)
            return null;
        return prop.getValue();
    }
}
