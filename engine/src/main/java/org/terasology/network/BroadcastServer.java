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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastServer extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastServer.class);
    private static final String DISCOVERY_REQUEST = "DISCOVERY_REQUEST";

    private DatagramSocket sendSocket;


    @Override
    public void run() {
        try {
            // Discovery request command
            byte[] data = DISCOVERY_REQUEST.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 8002);
            getSendSocket().send(packet);
            logger.info("Discovery package sent!" + packet.getAddress() + ":" + packet.getPort());
        } catch (IOException e) {
            logger.error("Broadcast Exception Encountered" + e.getMessage());
        }
    }

    public void startBroadcast() {
        Timer timer = new Timer(true);
        timer.schedule(new BroadcastServer(), 0, 1500);
    }

    public void stopBroadcast() {
        logger.info("Shutting down Broadcast");
        sendSocket.close();
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

}
