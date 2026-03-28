// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.heroiclabs.nakama.AbstractSocketListener;
import com.heroiclabs.nakama.Channel;
import com.heroiclabs.nakama.ChannelType;
import com.heroiclabs.nakama.Client;
import com.heroiclabs.nakama.DefaultClient;
import com.heroiclabs.nakama.Session;
import com.heroiclabs.nakama.SocketClient;
import com.heroiclabs.nakama.api.ChannelMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the Nakama connection.
 * Requires a running Nakama server — skipped in normal builds.
 *
 * The SDK uses two ports: gRPC for API calls (auth, etc.) and WebSocket for
 * realtime (chat, presence). When using NodePort, these are different ports.
 *
 * Run with: ./gradlew :subsystems:Nakama:test -PincludeTags=integration
 *           -Dnakama.test.host=localhost
 *           -Dnakama.test.grpcPort=7349
 *           -Dnakama.test.wsPort=7350
 */
@Tag("integration")
class NakamaIntegrationTest {

    private static final String TEST_CHANNEL = "bifrost.test." + UUID.randomUUID().toString().substring(0, 8);

    private final String host = System.getProperty("nakama.test.host", "localhost");
    private final int grpcPort = Integer.parseInt(System.getProperty("nakama.test.grpcPort", "7349"));
    private final int wsPort = Integer.parseInt(System.getProperty("nakama.test.wsPort", "7350"));

    private Client senderClient;
    private Client receiverClient;
    private SocketClient senderSocket;
    private SocketClient receiverSocket;

    @AfterEach
    void cleanup() {
        if (senderSocket != null) try { senderSocket.disconnect(); } catch (Exception ignored) { }
        if (receiverSocket != null) try { receiverSocket.disconnect(); } catch (Exception ignored) { }
    }

    @Test
    void canAuthenticateAndJoinChannel() throws Exception {
        senderClient = new DefaultClient("defaultkey", host, grpcPort, false);
        Session session = senderClient.authenticateDevice(UUID.randomUUID().toString()).get();

        assertNotNull(session.getUserId(), "Should receive a user ID after auth");
        assertFalse(session.IsExpired(), "Session should not be expired");

        senderSocket = senderClient.createSocket(host, wsPort, false);
        senderSocket.connect(session, new AbstractSocketListener() {}).get();

        Channel channel = senderSocket.joinChat(TEST_CHANNEL, ChannelType.ROOM).get();
        assertNotNull(channel.getId(), "Should receive a channel ID after joining");
    }

    @Test
    void canSendAndReceiveMessage() throws Exception {
        // Set up receiver first
        receiverClient = new DefaultClient("defaultkey", host, grpcPort, false);
        Session receiverSession = receiverClient.authenticateDevice(UUID.randomUUID().toString()).get();

        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<String> receivedContent = new AtomicReference<>();

        receiverSocket = receiverClient.createSocket(host, wsPort, false);
        receiverSocket.connect(receiverSession, new AbstractSocketListener() {
            @Override
            public void onChannelMessage(ChannelMessage message) {
                receivedContent.set(message.getContent());
                messageLatch.countDown();
            }
        }).get();

        Channel receiverChannel = receiverSocket.joinChat(TEST_CHANNEL, ChannelType.ROOM).get();

        // Set up sender
        senderClient = new DefaultClient("defaultkey", host, grpcPort, false);
        Session senderSession = senderClient.authenticateDevice(UUID.randomUUID().toString()).get();

        senderSocket = senderClient.createSocket(host, wsPort, false);
        senderSocket.connect(senderSession, new AbstractSocketListener() {}).get();
        senderSocket.joinChat(TEST_CHANNEL, ChannelType.ROOM).get();

        // Send a message
        JsonObject payload = new JsonObject();
        payload.addProperty("game", "terasology");
        payload.addProperty("player", "TestAlice");
        payload.addProperty("text", "Hello from integration test!");
        senderSocket.writeChatMessage(receiverChannel.getId(), payload.toString()).get();

        // Wait for receiver to get the message
        boolean received = messageLatch.await(10, TimeUnit.SECONDS);
        assertTrue(received, "Should receive message within 10 seconds");

        // Verify message content
        JsonObject parsed = JsonParser.parseString(receivedContent.get()).getAsJsonObject();
        assertEquals("terasology", parsed.get("game").getAsString());
        assertEquals("TestAlice", parsed.get("player").getAsString());
        assertEquals("Hello from integration test!", parsed.get("text").getAsString());
    }
}
