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

    // self.status_variables['AC_FUN_ENABLE'] = ""

    // self.status_variables['AC_FUN_POWER'] = ""
    public boolean AcFunPower = false;
    // self.status_variables['AC_FUN_SUPPORTED'] = ""
    // self.status_variables['AC_FUN_OPMODE'] = ""
    public Integer AcFunOpMode = 0;
    // self.status_variables['AC_FUN_TEMPSET'] = ""
    public Integer AcFunTempSet = 25;
    // self.status_variables['AC_FUN_COMODE'] = ""
    public Integer AcFunCoMode = 0;
    // self.status_variables['AC_FUN_ERROR'] = ""
    // self.status_variables['AC_FUN_TEMPNOW'] = ""
    public Integer AcFunTempNow = 0;
    // self.status_variables['AC_FUN_SLEEP'] = ""
    // self.status_variables['AC_FUN_WINDLEVEL'] = ""
    public Integer AcFunWindLevel = 0;
    // self.status_variables['AC_FUN_DIRECTION'] = ""
    public Integer AcFunDirection = 0;
    // self.status_variables['AC_ADD_AUTOCLEAN'] = ""
    public boolean AcAddAutoClean = false;
    // self.status_variables['AC_ADD_APMODE_END'] = ""
    // public boolean AcAddApmodeEnd = false;
    // self.status_variables['AC_ADD_STARTWPS'] = ""
    // public boolean AcAddStartWps = false;
    // self.status_variables['AC_ADD_SPI'] = ""
    // public boolean AcAddSpi = false;
    // self.status_variables['AC_SG_WIFI'] = ""
    // public boolean AcSgWifi = false;
    // self.status_variables['AC_SG_INTERNET'] = ""
    // public boolean AcSgInternet = false;
    // self.status_variables['AC_ADD2_VERSION'] = ""
    // self.status_variables['AC_SG_MACHIGH'] = ""
    // self.status_variables['AC_SG_MACMID'] = ""
    // self.status_variables['AC_SG_MACLOW'] = ""
    // self.status_variables['AC_SG_VENDER01'] = ""
    // self.status_variables['AC_SG_VENDER02'] = ""
    // self.status_variables['AC_SG_VENDER03'] = ""
}
