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

import org.w3c.dom.Element;

/**
 * The {@link SynchronizedSamsungACCommandResponse} is responsible for handling syncronization from the read thread to
 * the
 * main thread of the binding
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SynchronizedSamsungACCommandResponse {
    SamsungACCommandResponse response;
    private volatile boolean isResponseReceived = false;

    public SynchronizedSamsungACCommandResponse() {
        response = null;
    }

    public synchronized void setResponse(boolean isOk, LocalDateTime timestamp, Element xmlElement) {

        this.setResponse(new SamsungACCommandResponse(isOk, timestamp, xmlElement));
    }

    public synchronized void setResponse(SamsungACCommandResponse response) {
        /*
         * while (this.isResponseReceived) {
         * try {
         * wait();
         * } catch (InterruptedException e) {
         * // e.printStackTrace();
         * }
         * }
         */
        this.isResponseReceived = true;
        this.response = response;
        notify();
    }

    public synchronized SamsungACCommandResponse getResponse() {
        while (!this.isResponseReceived) {
            try {
                wait();
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
        SamsungACCommandResponse newResponse = response;
        this.isResponseReceived = false;
        notify();
        return newResponse;
    }
}
