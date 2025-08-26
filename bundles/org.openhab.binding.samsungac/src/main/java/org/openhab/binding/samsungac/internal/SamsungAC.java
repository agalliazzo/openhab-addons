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
import java.util.*;

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
    @Nullable
    public final SamsungACProperties properties = new SamsungACProperties();

    // public final Map<String, SamsungACPropertyChangedCallback> callbacks = new HashMap<String,
    // SamsungACPropertyChangedCallback>();
    private SamsungACPropertyChangedCallback propertyChangedCallback = null;

    public SamsungAC(InetAddress ipAddress, String UID, String token) {
        this.ipAddress = ipAddress;
        this.UID = UID;
        this.token = token;
    }

    public SamsungAC(String hostname, String UID, String token) throws UnknownHostException {
        this(InetAddress.getByName(hostname), UID, token);
    }

    public void setCallback(SamsungACPropertyChangedCallback callback) {
        propertyChangedCallback = callback;
        if (listeningThread != null) {
            listeningThread.setCallback(callback);
        }
    }

    public void removeCallback(String property) {
        propertyChangedCallback = null;
        if (listeningThread != null) {
            listeningThread.setCallback(null);
        }
    }

    public static class ListeningThreadClass extends Thread {
        public final SynchronizedSamsungACCommandResponse commandResponse = new SynchronizedSamsungACCommandResponse();
        private final Logger logger = LoggerFactory.getLogger(ListeningThreadClass.class);
        private final SamsungACProperties properties;
        private InputStream inputStream;
        private boolean exitThread = false;
        private SamsungACPropertyChangedCallback propertyChangedCallback;

        public ListeningThreadClass(InputStream inputStream, SamsungACProperties properties,
                SamsungACPropertyChangedCallback callback) {
            this.inputStream = inputStream;
            this.properties = properties;
            this.propertyChangedCallback = callback;
        }

        public void setCallback(SamsungACPropertyChangedCallback callback) {
            this.propertyChangedCallback = callback;
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
                case "AC_FUN_POWER", "AC_ADD_AUTOCLEAN", "AC_ADD_SPI":
                    properties.setValue(propertyName, value.equals("On"));
                    break;
                case "AC_SG_INTERNET":
                    properties.setValue(propertyName, value.equals("Connected"));
                    break;
                case "AC_FUN_OPMODE", "AC_FUN_DIRECTION", "AC_FUN_WINDLEVEL", "AC_FUN_COMODE":
                    properties.setValue(propertyName, value);
                    break;
                case "AC_FUN_TEMPSET", "AC_FUN_TEMPNOW":
                    properties.setValue(propertyName, Integer.parseInt(value));
                    break;
                default:
                    logger.warn("Unmanaged property: {}", propertyName);
            }
            if (propertyChangedCallback != null) {
                propertyChangedCallback.onPropertyChanged(propertyName, properties.getValue(propertyName));
            }
        }

        public void quit() {
            this.interrupt();
        }
    }

    public void connect() throws SamsungACConnectionFailedException {
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

            listeningThread = new ListeningThreadClass(socket.getInputStream(), this.properties,
                    this.propertyChangedCallback);
            listeningThread.start();

        } catch (UnknownHostException e) {
            logger.error("Error connecting to Samsung AC", e);
            throw new SamsungACConnectionFailedException(e);
        } catch (IOException e) {
            logger.error("IOException Error connecting to Samsung AC", e);
            throw new SamsungACConnectionFailedException(e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("No such algorithm", e);
            throw new SamsungACConnectionFailedException(e);
        } catch (KeyManagementException e) {
            logger.error("Key management exception", e);
            throw new SamsungACConnectionFailedException(e);
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

    public void authenticate() throws SamsungACAuthenticationFailedException {
        sendCommand("<Request Type=\"AuthToken\"><User Token=\"" + token + "\"/></Request>");
        if (waitForCommandResponse()) {
            logger.info("Authentication successful");
        } else {
            logger.error("Authentication failed");
            throw new SamsungACAuthenticationFailedException();
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

    public void virusDoctor(boolean enabled) {
        sendCommand("AC_ADD_SPI", enabled ? "On" : "Off");
    }

    public void virusDoctorOn() {
        virusDoctor(true);
    }

    public void virusDoctorOff() {
        virusDoctor(false);
    }

    public void setWindLevel(String level) {
        sendCommand("AC_FUN_WINDLEVEL", level);
    }

    public void setWindLevel(int level) {
        if (level > SamsungACProperties.fanLevels.length)
            throw new IllegalArgumentException("Fan level out of bounds");
        setWindLevel(SamsungACProperties.fanLevels[level]);
    }

    public void setWorkMode(String mode) {
        sendCommand("AC_FUN_OPMODE", mode);
    }

    public void setWorkMode(int mode) {
        if (mode > SamsungACProperties.workModes.length)
            throw new IllegalArgumentException("Work mode out of bounds");
        setWorkMode(SamsungACProperties.workModes[mode]);
    }

    public void setCoolingMode(String mode) {
        sendCommand("AC_FUN_COMODE", mode);
    }

    public void setCoMode(int mode) {
        if (mode > SamsungACProperties.coModes.length)
            throw new IllegalArgumentException("Cooling mode out of bounds");
        setCoolingMode(SamsungACProperties.coModes[mode]);
    }

    public void setFanDirection(String direction) {
        sendCommand("AC_FUN_DIRECTION", direction);
    }

    public void setFanDirection(int direction) {
        if (direction > SamsungACProperties.fanDirection.length)
            throw new IllegalArgumentException("Fan direction out of bounds");
        setFanDirection(SamsungACProperties.fanDirection[direction]);
    }

    public void setTemperature(int temperature) {
        sendCommand("AC_FUN_TEMPSET", String.valueOf(temperature));
    }
}
