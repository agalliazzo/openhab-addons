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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link SamsungAC} is responsible for handling communication with the AC unit
 *
 * @author Alessio Galliazzo - Initial contribution
 */
public class SamsungAC {
    private final Logger logger = LoggerFactory.getLogger(SamsungAC.class);
    @Nullable
    private ListeningThreadClass listeningThread = null;
    private final InetAddress ipAddress;
    private final String UID;
    private final String token;
    @Nullable
    private SSLSocket socket = null;

    public final SamsungACProperties properties = new SamsungACProperties();

    public SamsungAC(InetAddress ipAddress, String UID, String token) {
        this.ipAddress = ipAddress;
        this.UID = UID;
        this.token = token;
    }

    public SamsungAC(String hostname, String UID, String token) throws UnknownHostException {
        this(InetAddress.getByName(hostname), UID, token);
    }

    public static class ListeningThreadClass extends Thread {
        public final SynchronizedSamsungACCommandResponse commandResponse = new SynchronizedSamsungACCommandResponse();
        final Logger logger = LoggerFactory.getLogger(ListeningThreadClass.class);
        final SamsungACProperties properties;
        InputStream inputStream;
        boolean exitThread = false;

        public ListeningThreadClass(InputStream inputStream, SamsungACProperties properties) {
            this.inputStream = inputStream;
            this.properties = properties;
        }

        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while (!exitThread) {
                try {
                    String data = reader.readLine();
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    InputSource is = new InputSource(new StringReader(data));
                    Document doc = builder.parse(is);
                    Element root = doc.getDocumentElement();
                    if (root.getTagName().equals("Response")) {
                        logger.debug("Received response: {}", data);
                        processResponse(root);
                    } else if (root.getTagName().equals("Update")) {
                        logger.info("Received status: {}", data);
                        proceessUpdate(root);
                    }

                } catch (java.net.SocketTimeoutException e) {
                    logger.debug("Socket timeout");
                } catch (IOException e) {
                    logger.error("IOException Error listening to Samsung AC", e);
                } catch (ParserConfigurationException e) {
                    logger.error("Error parsing response", e);
                } catch (SAXException e) {
                    logger.error("Error parsing response", e);
                }

            }
        }

        private void processResponse(Element response) {
            String status = response.getAttribute("Status");
            String startFrom = response.getAttribute("StartFrom").replaceAll("/", "T") + "Z";
            LocalDateTime timestamp = LocalDateTime.parse(startFrom, DateTimeFormatter.ISO_DATE_TIME);
            synchronized (commandResponse) {
                commandResponse.setResponse(status.equals("Okay"), timestamp);
                // setResponse("Okay".equals(status), timestamp);
            }
        }

        @Nullable
        public synchronized SamsungACCommandResponse getResponse() {
            synchronized (commandResponse) {
                return commandResponse.getResponse();
            }
        }

        private void proceessUpdate(Element update) {
            // <?xml version="1.0" encoding="utf-8" ?>
            // <Update Type="Status">
            // <Status DUID="7825AD11729B0000" GroupID="AC" ModelID="AC">
            // <Attr ID="AC_FUN_POWER" Value="On" />
            // </Status>
            // </Update>
            if (!"Status".equals(update.getAttribute("Type")))
                return;
            Element status = (Element) update.getElementsByTagName("Status").item(0);
            Element attr = (Element) status.getElementsByTagName("Attr").item(0);
            String propertyName = attr.getAttribute("ID");
            String value = attr.getAttribute("Value");
            switch (propertyName) {
                case "AC_FUN_POWER":
                    properties.AcFunPower = value.equals("On");
                    break;
                case "AC_FUN_OPMODE":
                    properties.AcFunOpMode = IntStream.range(0, SamsungACProperties.workModes.length)
                            .filter(i -> SamsungACProperties.workModes[i].equals(value)).findFirst().orElse(-1);
                    break;
                case "AC_FUN_TEMPSET":
                    properties.AcFunTempSet = Integer.parseInt(value);
                    break;
                case "AC_FUN_COMODE":
                    properties.AcFunCoMode = IntStream.range(0, SamsungACProperties.coModes.length)
                            .filter(i -> SamsungACProperties.coModes[i].equals(value)).findFirst().orElse(-1);
                    break;
                case "AC_FUN_TEMPNOW":
                    properties.AcFunTempNow = Integer.parseInt(value);
                    break;
                case "AC_FUN_WINDLEVEL":
                    properties.AcFunWindLevel = IntStream.range(0, SamsungACProperties.fanLevels.length)
                            .filter(i -> SamsungACProperties.fanLevels[i].equals(value)).findFirst().orElse(-1);
                    break;
                case "AC_FUN_DIRECTION":
                    properties.AcFunDirection = IntStream.range(0, SamsungACProperties.fanDirection.length)
                            .filter(i -> SamsungACProperties.fanDirection[i].equals(value)).findFirst().orElse(-1);
                    ;
                    break;
                case "AC_ADD_AUTOCLEAN":
                    properties.AcAddAutoClean = value.equals("On");
                    break;
                default:
                    logger.warn("Unmanaged property: {}", propertyName);
            }
        }

        public void quit() {
            this.interrupt();
        }
    }

    public void connect() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            final X509TrustManager[] trustAllCerts = new X509TrustManager[] { new TrustAll() };
            sslContext.init(null, trustAllCerts, null);

            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            this.socket = (SSLSocket) socketFactory.createSocket(this.ipAddress, 2878);
            String[] cipherSuites = socket.getSupportedCipherSuites();
            List<String> filteredCiphers = Arrays.stream(cipherSuites).filter(cipher -> !cipher.contains("DH"))
                    .toList();

            socket.setEnabledProtocols(new String[] { "TLSv1" });
            socket.setEnabledCipherSuites(filteredCiphers.toArray(new String[0]));
            socket.setSoTimeout(10000);
            socket.startHandshake();

            listeningThread = new ListeningThreadClass(socket.getInputStream(), this.properties);
            listeningThread.start();

        } catch (UnknownHostException e) {
            logger.error("Error connecting to Samsung AC", e);
        } catch (IOException e) {
            logger.error("IOException Error connecting to Samsung AC", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("No such algorithm", e);
        } catch (KeyManagementException e) {
            logger.error("Key management exception", e);
        }
    }

    private void sendCommand(String command) {
        try {
            assert socket != null;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(command + "\n\r");
            writer.flush();
        } catch (IOException e) {
            logger.error("IOException Error sending command to Samsung AC", e);
        }
    }

    private void sendCommand(String Key, String Value) {
        Integer msgId = new Random().nextInt(10000);
        String commandId = String.format("cmd%04d", msgId);
        String command = String.format("<Request Type=\"DeviceControl\">" + "<Control CommandID=\"%s\" DUID=\"%s\">"
                + "<Attr ID=\"%s\" Value=\"%s\"/>" + "</Control>" + "</Request>", commandId, UID, Key, Value);
        this.sendCommand(command);
    }

    private boolean waitForCommandResponse() {
        assert listeningThread != null;
        SamsungACCommandResponse response = listeningThread.getResponse();
        while (response == null) {
            response = listeningThread.getResponse();
        }
        return response.isOk();
    }

    // TODO: Make this method throw a specific exception
    public void authenticate() {
        sendCommand("<Request Type=\"AuthToken\"><User Token=\"" + token + "\"/></Request>");
        if (waitForCommandResponse()) {
            logger.info("Authentication successful");
        } else {
            logger.error("Authentication failed");
        }
    }

    public void power(boolean on) {
        sendCommand("AC_FUN_POWER", on ? "On" : "Off");
    }

    public void powerOn() {
        power(true);
    }

    public void powerOff() {
        power(false);
    }

    public void virus_doctor(boolean enabled) {
        sendCommand("AC_ADD_SPI", enabled ? "On" : "Off");
    }

    public void set_fan_level(int level) {
        if (level > SamsungACProperties.fanLevels.length)
            throw new IllegalArgumentException("Fan level out of bounds");
        sendCommand("AC_FUN_FAN", SamsungACProperties.fanLevels[level]);
    }

    public void set_work_mode(int mode) {
        if (mode > SamsungACProperties.workModes.length)
            throw new IllegalArgumentException("Work mode out of bounds");
        sendCommand("AC_FUN_OPMODE", SamsungACProperties.workModes[mode]);
    }

    public void set_co_mode(int mode) {
        if (mode > SamsungACProperties.coModes.length)
            throw new IllegalArgumentException("Cooling mode out of bounds");
        sendCommand("AC_FUN_COMODE", SamsungACProperties.coModes[mode]);
    }

    public void set_fan_direction(int direction) {
        if (direction > SamsungACProperties.fanDirection.length)
            throw new IllegalArgumentException("Fan direction out of bounds");
        sendCommand("AC_FUN_DIRECTION", SamsungACProperties.fanDirection[direction]);
    }

    public void set_temperature(int temperature) {
        sendCommand("AC_FUN_TEMPSET", String.valueOf(temperature));
    }
}
