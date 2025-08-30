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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The {@link SamsungACProperty} class contains the property of the Samsung AC.
 *
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SamsungACProperty {
    private Object value;
    volatile boolean isUpdated = false;
    private Lock dataLock = new ReentrantLock();

    public SamsungACProperty() {
    }

    public SamsungACProperty(Object value) {
        this.value = value;
    }

    public synchronized Object getValue() {
        while (!isUpdated) {
            try {
                wait(5000);
            } catch (InterruptedException ignored) {

            }
        }
        Object ret = value;
        isUpdated = false;
        notify();
        return ret;
    }

    public synchronized void setValue(Object value) {
        while (isUpdated) {
            try {
                wait();
            } catch (InterruptedException ignored) {

            }
        }
        this.value = value;
        isUpdated = true;
        notify();
    }
}
