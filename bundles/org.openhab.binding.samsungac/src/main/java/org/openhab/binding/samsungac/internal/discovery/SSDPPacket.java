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

import java.util.HashMap;

/**
 * The {@link SSDPPacket} manage a message and store in a more valuable way
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SSDPPacket extends HashMap<String, String> {

    static public SSDPPacket fromMessage(String Message) {
        String[] lines = Message.split("\r\n");
        SSDPPacket packet = new SSDPPacket();
        for (String line : lines) {
            int firstColonPosition = line.indexOf(":");
            if (firstColonPosition != -1) {
                String key = line.substring(0, firstColonPosition).strip();
                String value = line.substring(firstColonPosition + 1).strip();
                packet.put(key, value);
                // return packet;
            }
        }
        return packet;
    }
}
