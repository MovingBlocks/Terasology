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
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastClient extends TimerTask{

    private static final Logger logger = LoggerFactory.getLogger(BroadcastServer.class);
    private static final String DISCOVERY_REQUEST= "DISCOVERY_REQUEST";

    private DatagramSocket receiveSocket;

    @Override
    public void run(){
        try {
            byte[] buffer = DISCOVERY_REQUEST.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            getReceiveSocket().receive(packet);
            logger.info("Discovery package received! -> " + packet.getAddress() + ":" + packet.getPort());
        } catch (IOException e) {
            logger.error("Broadcast Exception Encountered" + e.getMessage());
        }
    }

    public void startBroadcast() {
        Timer timer = new Timer();
        timer.schedule(new BroadcastClient(), 0, 1500);
    }

    public void stopBroadCast() {
        logger.info("Shutting down Broadcast");
        receiveSocket.close();
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
}
