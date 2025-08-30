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

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungACSSDPListener} listens for SSDP messages from the Samsung AC.
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SamsungACSSDPListener implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(SamsungACSSDPListener.class);
    private static final String MulticastAddress = "239.255.255.250";
    private static final int MulticastPort = 1900;
    private final SamsungACDeviceDiscovered deviceDiscovered;
    private volatile boolean running = true;

    public SamsungACSSDPListener(SamsungACDeviceDiscovered deviceDiscoveredCallback) {
        deviceDiscovered = deviceDiscoveredCallback;
    }

    private InetAddress[] getMulticastAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback() && networkInterface.supportsMulticast()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address inet4Address) {
                            byte[] bytes = inet4Address.getAddress();
                            bytes[3] = (byte) 255;
                            addresses.add(Inet4Address.getByAddress(bytes));
                        }
                        if (inetAddress.isMulticastAddress())
                            addresses.add(inetAddress);
                    }
                }
            }

        } catch (SocketException e) {
        } catch (UnknownHostException e) {

        }
        return addresses.toArray(new InetAddress[0]);
    }

    public void sendNotify() {
        String mcastDest = "192.168.2.255";
        String message = """
                NOTIFY * HTTP/1.1\r
                HOST: 239.255.255.250:1900\r
                CACHE-CONTROL: max-age=20\r
                SERVER: AIR CONDITIONER\r
                \r
                SPEC_VER: MSpec-1.00\r
                SERVICE_NAME: ControlServer-MLib\r
                MESSAGE_TYPE: CONTROLLER_START\r
                """;

        InetAddress[] addresses = getMulticastAddresses();
        for (InetAddress address : addresses) {
            logger.info("Sending SSDP notify to {}", address);
            try (MulticastSocket socket = new MulticastSocket()) {
                // InetAddress group = InetAddress.getByName(mcastDest);
                // socket.joinGroup(group);
                socket.send(new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.length(), address,
                        MulticastPort));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        running = true;
        try (MulticastSocket socket = new MulticastSocket(MulticastPort)) {
            InetAddress group = InetAddress.getByName(MulticastAddress);
            socket.joinGroup(group);
            socket.setSoTimeout(2500);
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (running) {
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ignored) {

                }

                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.contains("NOTIFY * HTTP/1.1") && message.contains("SERVER: SSDP,SAMSUNG")) {
                    SSDPPacket ssdpPacket = SSDPPacket.fromMessage(message);
                    deviceDiscovered.deviceDiscovered(ssdpPacket);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running = false;
    }
}
