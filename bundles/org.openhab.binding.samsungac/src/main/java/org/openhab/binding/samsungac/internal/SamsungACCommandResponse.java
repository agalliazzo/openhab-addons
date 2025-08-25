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

import java.time.LocalDateTime;

/**
 * The {@link SamsungACCommandResponse} contains the response of a command
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SamsungACCommandResponse {
    private final boolean isOk;
    private final LocalDateTime timestamp;

    public SamsungACCommandResponse(boolean isOk, LocalDateTime timestamp) {
        this.isOk = isOk;
        this.timestamp = timestamp;
    }

    public boolean isOk() {
        return isOk;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
