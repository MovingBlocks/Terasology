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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadCastServer {

    private static boolean turnOnBroadcast;
    private static final Logger logger = LoggerFactory.getLogger(BroadCastServer.class);
    private static final String DISCOVERY_REQUEST = "DISCOVERY_REQUEST";
    private static final String DISCOVERY_RESPONSE = "DISCOVERY_RESPONSE";
    private  DatagramSocket receiveSocket;
    private  DatagramSocket sendSocket;


    public boolean isBroadCastTurnedOn() {
        return turnOnBroadcast;
    }

    public void startBroadcast() {
        BroadCastServer.turnOnBroadcast = true;
        try {
            sendBroadCast();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            while (turnOnBroadcast) {
                                listenToBroadCast();
                            }
                        } catch (Exception e) {
                            logger.error("Broadcasting has been interrupted" + e.getMessage());
                            stopBroadCast();
                        }
                    }
                }
            });
        } catch (Exception e) {
        logger.info("Broadcasting has been interrupted " + e.getMessage());
        stopBroadCast();
        }
    }

    public void stopBroadCast() {
        BroadCastServer.turnOnBroadcast = false;
        logger.info("Shutting down BroadCasting");
        this.sendSocket.close();
        this.receiveSocket.close();
    }

    private void listenToBroadCast() throws Exception {
        byte[] buffer = DISCOVERY_REQUEST.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        getReceiveSocket().receive(packet);
        logger.info("Discovery package received! -> " + packet.getAddress() + ":" + packet.getPort());

        //Validate the data sent.
        String data = new String(packet.getData()).trim();
        if (data.equals(DISCOVERY_REQUEST)) { // validate command
            // Send response
            byte[] response = new byte[DISCOVERY_RESPONSE.length()];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
            getReceiveSocket().send(responsePacket);
            logger.info("Response sent to: " + packet.getAddress() + ":" + packet.getPort());
        } else {
            logger.info("Error, not valid package!" + packet.getAddress() + ":" + packet.getPort());
        }
    }

    private DatagramSocket getReceiveSocket()  {
        try {
            if (receiveSocket == null) {
                receiveSocket = new DatagramSocket(8002,
                    InetAddress.getByName("0.0.0.0")); // 0.0.0.0 for listen to all ips
                receiveSocket.setBroadcast(true);
            }
        } catch (Exception e) {
            logger.error("Broadcast Exception Encountered" + e.getMessage());
        }
        return receiveSocket;
    }

    private DatagramSocket getSendSocket() {
        try {
            if (sendSocket == null) {
                sendSocket = new DatagramSocket(8001, InetAddress.getLocalHost());
                sendSocket.setBroadcast(true);
            }
        } catch (Exception e) {
            logger.error("Broadcast Exception Encountered" + e.getMessage());
        }
        return sendSocket;
    }

    public void sendBroadCast() throws Exception {
        // Discovery request command
        byte[] data = DISCOVERY_REQUEST.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 8002);
        getSendSocket().send(packet);
        logger.info("Discovery package sent!" + packet.getAddress() + ":" + packet.getPort());

        // Discovery response command
        byte[] response = new byte[DISCOVERY_RESPONSE.length()];
        DatagramPacket responsePacket = new DatagramPacket(response, response.length);
        getSendSocket().receive(responsePacket);
        logger.info("Discovery response received!" + responsePacket.getAddress() + ":" + responsePacket.getPort());
        String responseData = new String(responsePacket.getData(), StandardCharsets.UTF_8);
        if (responseData.equals(DISCOVERY_RESPONSE)) { // Check valid command
            logger.info("Found server!" + responsePacket.getAddress() + ":" + responsePacket.getPort());
        }
    }
}