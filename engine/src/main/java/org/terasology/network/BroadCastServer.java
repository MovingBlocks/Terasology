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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.layers.mainMenu.JoinGameScreen;


public class BroadCastServer {

    private static DatagramSocket receiveSocket;
    private static DatagramSocket sendSocket;
    private static boolean turnOnBroadcast;
    private static final Logger logger = LoggerFactory.getLogger(JoinGameScreen.class);
    private static ScheduledExecutorService service;

    public boolean isBroadCastTurnedOn() {
        return turnOnBroadcast;
    }

    public void setTurnOnBroadcast(boolean turnOnBroadcast) {
        BroadCastServer.turnOnBroadcast = turnOnBroadcast;
    }

    public void startBroadcast() {
        try {
            sendBroadCast();
            service = Executors.newSingleThreadScheduledExecutor();
            Runnable runnable = new Runnable() {
                public void run() {
                    logger.info("Listening to broadcast");
                    try {
                        listenToBroadCast();
                    } catch (Exception e) {
                        logger.error("Broadcasting has been interrupted" + e.getMessage());
                    }
                }
            };
            service.scheduleAtFixedRate(runnable, 0, 300, TimeUnit.SECONDS);
        } catch (Exception e) {
        logger.info("Broadcasting has been interrupted " + e.getMessage());
        e.printStackTrace();
        }
    }

    public void stopBroadCast() {
        logger.info("Shutting down BroadCasting");
        service.shutdown();
    }

    private void listenToBroadCast() throws Exception {
        byte[] buffer = "__DISCOVERY_REQUEST__".getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        getReceiveSocket().receive(packet);
        logger.info("Discovery package received! -> " + packet.getAddress() + ":" + packet.getPort());

        //Validate the data sent.
        String data = new String(packet.getData()).trim();
        if (data.equals("__DISCOVERY_REQUEST__")) { // validate command
            // Send response
            byte[] response = new byte["__DISCOVERY_RESPONSE".length()];
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
        byte[] data = "__DISCOVERY_REQUEST__".getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 8002);
        getSendSocket().send(packet);
        logger.info("Discovery package sent!" + packet.getAddress() + ":" + packet.getPort());

        // Discovery response command
        byte[] response = new byte["__DISCOVERY_RESPONSE__".length()];
        DatagramPacket responsePacket = new DatagramPacket(response, response.length);
        getSendSocket().receive(responsePacket);
        logger.info("Discovery response received!" + responsePacket.getAddress() + ":" + responsePacket.getPort());
        String responseData = new String(responsePacket.getData()).trim();
        if (responseData.equals("__DISCOVERY_RESPONSE__")) { // Check valid command
            logger.info("Found server!" + responsePacket.getAddress() + ":" + responsePacket.getPort());
        }
    }
}