/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.rendering.nui.layers.mainMenu.JoinGameScreen;


public class BroadCastServer {

    private static DatagramSocket socket;


    private static boolean turnOnBroadcast;
    private static final Logger logger = LoggerFactory.getLogger(JoinGameScreen.class);
    private static ScheduledExecutorService service;
    private Config config;



    public boolean isBroadCastTurnedOn() {
        return turnOnBroadcast;
    }

    public void setTurnOnBroadcast(boolean turnOnBroadcast) {
        BroadCastServer.turnOnBroadcast = turnOnBroadcast;
    }

    public void startBroadcast() {
        service = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    logger.info("Broadcasting message");
                        broadCast();
                } catch (Exception e) {
                    logger.info("Broadcasting has been interrupted " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        service.scheduleAtFixedRate(runnable, 0, 300, TimeUnit.SECONDS);
    }

    public void stopBroadCast() {
        logger.info("Shutting down BroadCasting");
        service.shutdown();
    }

    private void broadCast() throws Exception {
        final int ssdpPort = 1900;
        final int ssdpSearchPort = 1901;
        // Broadcast address for finding routers.
        final String ssdpIP = "239.255.255.250";
        int timeout = 5000;
        InetAddress localhost = InetAddress.getLocalHost();
        // Send from localhost:1901
        InetSocketAddress srcAddress = new InetSocketAddress(localhost, ssdpSearchPort);
        // Send to 239.255.255.250:1900
        InetSocketAddress dstAddress = new InetSocketAddress(InetAddress.getByName(ssdpIP), ssdpPort);
        // ----------------------------------------- //
        //       Construct the request packet.       //
        // ----------------------------------------- //
        StringBuffer discoveryMessage = new StringBuffer();
        discoveryMessage.append("M-SEARCH * HTTP/1.1\r\n");
        discoveryMessage.append("HOST: " + ssdpIP + ":" + ssdpPort + "\r\n");
        discoveryMessage.append("ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1\r\n");
        // ST: urn:schemas-upnp-org:service:WANIPConnection:1\r\n
        discoveryMessage.append("MAN: \"ssdp:discover\"\r\n");
        discoveryMessage.append("MX: 2\r\n");
        discoveryMessage.append("\r\n");
        byte[] discoveryMessageBytes = discoveryMessage.toString().getBytes();
        DatagramPacket discoveryPacket = new DatagramPacket(discoveryMessageBytes, discoveryMessageBytes.length, dstAddress);
        // ----------------------------------- //
        //       Send multi-cast packet.       //
        // ----------------------------------- //
        MulticastSocket multicast = null;
        try {
            multicast = new MulticastSocket(null);
            multicast.bind(srcAddress);
            multicast.setTimeToLive(4);
            System.out.println("Send multicast request.");
            // ----- Sending multi-cast packet ----- //
            multicast.send(discoveryPacket);
        } finally {
            System.out.println("Multicast ends. Close connection.");
            multicast.disconnect();
            multicast.close();
        }
    }

}
