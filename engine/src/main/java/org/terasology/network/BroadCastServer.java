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
import org.terasology.rendering.nui.layers.mainMenu.JoinGameScreen;


public class BroadCastServer {

    private static DatagramSocket socket;
    private static boolean turnOnBroadcast;
    private static final Logger logger = LoggerFactory.getLogger(JoinGameScreen.class);
    private NetworkSystem networkSystem;


    public boolean isTurnOnBroadcast() {
        return turnOnBroadcast;
    }

    public void setTurnOnBroadcast(boolean turnOnBroadcast) {
        BroadCastServer.turnOnBroadcast = turnOnBroadcast;
    }

    public void startBroadcast(String message) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    logger.info("Broadcasting message" + message);
                    for (InetAddress inadr : listAllBroadcastAddresses()) {
                        broadcast(message, inadr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 300, TimeUnit.SECONDS);
    }

    /**
     * @param broadcastMessage The broadcast Message
     * @param address address for which the message needs to be broadcasted
     */
    public void broadcast(
        String broadcastMessage, InetAddress address) throws IOException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);

        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
            = new DatagramPacket(buffer, buffer.length, address, 4445);
        socket.send(packet);
        socket.close();
    }

    /**
     * @return List of InetAddresses for the network interface
     */
    public static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
            = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                .map(a -> a.getBroadcast())
                .filter(Objects::nonNull)
                .forEach(broadcastList::add);
        }
        return broadcastList;
    }

}
