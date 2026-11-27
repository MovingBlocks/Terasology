// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.discordrpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscordRPCThreadTest {
    private static final long DISCORD_APP_CLIENT_ID = 515274721080639504L;

    @Mock
    private IPCClient ipcClient;

    private DiscordRPCThread rpcThread;

    @AfterEach
    void tearDown() throws InterruptedException {
        if (rpcThread != null && rpcThread.isThreadAlive()) {
            rpcThread.stop();
            rpcThread.joinThread(2000L);
        }
    }

    @Test
    void wrappedSocketExceptionRetriesAndWaitsWithoutKillingThread() throws Exception {
        doThrow(new RuntimeException(new SocketException("No such file or directory; errno=2")))
                .when(ipcClient).connect();

        rpcThread = new DiscordRPCThread(ipcClient, 1L);
        rpcThread.enable();
        rpcThread.start();

        awaitTrue(() -> !rpcThread.isAutoReconnect() && rpcThread.isWaiting(), 2000L);

        verify(ipcClient, times(DiscordRPCThread.MAX_RECONNECT_TRIES + 1)).connect();
        assertThat(rpcThread.isThreadAlive()).isTrue();
        assertThat(rpcThread.isConnected()).isFalse();
    }

    @Test
    void wrappedFileNotFoundExceptionRetriesAndWaitsWithoutKillingThread() throws Exception {
        doThrow(new RuntimeException(new FileNotFoundException("\\\\?\\pipe\\discord-ipc-0")))
                .when(ipcClient).connect();

        rpcThread = new DiscordRPCThread(ipcClient, 1L);
        rpcThread.enable();
        rpcThread.start();

        awaitTrue(() -> !rpcThread.isAutoReconnect() && rpcThread.isWaiting(), 2000L);

        verify(ipcClient, times(DiscordRPCThread.MAX_RECONNECT_TRIES + 1)).connect();
        assertThat(rpcThread.isThreadAlive()).isTrue();
    }

    @Test
    void discoverWakesWaitingThreadAndConnectsWhenDiscordBecomesAvailable() throws Exception {
        AtomicReference<DiscordRPCThread> threadRef = new AtomicReference<>();
        CountDownLatch connectedLatch = new CountDownLatch(1);

        doThrow(new RuntimeException(new SocketException("No such file or directory; errno=2")))
                .when(ipcClient).connect();

        rpcThread = new DiscordRPCThread(ipcClient, 1L);
        threadRef.set(rpcThread);
        rpcThread.enable();
        rpcThread.start();

        awaitTrue(() -> !rpcThread.isAutoReconnect() && rpcThread.isWaiting(), 2000L);

        when(ipcClient.getStatus()).thenReturn(PipeStatus.CONNECTED);
        doAnswer(invocation -> {
            threadRef.get().onReady(ipcClient);
            return null;
        }).when(ipcClient).connect();
        doAnswer(invocation -> {
            connectedLatch.countDown();
            return null;
        }).when(ipcClient).sendRichPresence(any(RichPresence.class));

        rpcThread.getBuffer().setState("In Main Menu");
        rpcThread.discover();

        assertThat(connectedLatch.await(2000L, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(rpcThread.isConnected()).isTrue();
    }

    @Test
    void interruptDuringRetrySleepDoesNotCloseDisconnectedClient() throws Exception {
        CountDownLatch attemptLatch = new CountDownLatch(1);
        when(ipcClient.getStatus()).thenReturn(PipeStatus.UNINITIALIZED);
        doAnswer(invocation -> {
            attemptLatch.countDown();
            throw new NoDiscordClientException();
        }).when(ipcClient).connect();

        rpcThread = new DiscordRPCThread(ipcClient, 5000L);
        rpcThread.enable();
        rpcThread.start();

        assertThat(attemptLatch.await(2000L, TimeUnit.MILLISECONDS)).isTrue();
        awaitTrue(() -> rpcThread.getThreadState() == Thread.State.TIMED_WAITING, 2000L);

        rpcThread.stop();
        rpcThread.joinThread(2000L);

        assertThat(rpcThread.isThreadAlive()).isFalse();
        verify(ipcClient, never()).close();
    }

    @Test
    void concurrentDisconnectDuringSendRichPresenceResetsConnectedWithoutCrashing() throws Exception {
        AtomicReference<DiscordRPCThread> threadRef = new AtomicReference<>();
        CountDownLatch sentLatch = new CountDownLatch(1);

        when(ipcClient.getStatus()).thenReturn(PipeStatus.CONNECTED);
        doAnswer(invocation -> {
            threadRef.get().onReady(ipcClient);
            return null;
        }).when(ipcClient).connect();
        doAnswer(invocation -> {
            sentLatch.countDown();
            throw new IllegalStateException("IPCClient is not connected!");
        }).when(ipcClient).sendRichPresence(any());

        rpcThread = new DiscordRPCThread(ipcClient, 1L);
        threadRef.set(rpcThread);
        rpcThread.enable();
        rpcThread.start();

        assertThat(sentLatch.await(2000L, TimeUnit.MILLISECONDS)).isTrue();
        awaitTrue(() -> !rpcThread.isConnected(), 2000L);
        assertThat(rpcThread.isThreadAlive()).isTrue();
    }

    @Test
    void interruptDuringConnectClosesClientAndExitsThread() throws Exception {
        AtomicReference<DiscordRPCThread> threadRef = new AtomicReference<>();

        when(ipcClient.getStatus()).thenReturn(PipeStatus.CONNECTED);
        doAnswer(invocation -> {
            DiscordRPCThread current = threadRef.get();
            current.onReady(ipcClient);
            current.disable();
            current.stop();
            return null;
        }).when(ipcClient).connect();

        rpcThread = new DiscordRPCThread(ipcClient, 1L);
        threadRef.set(rpcThread);
        rpcThread.enable();
        rpcThread.start();

        rpcThread.joinThread(2000L);

        assertThat(rpcThread.isThreadAlive()).isFalse();
        verify(ipcClient, times(1)).close();
    }

    @Test
    void nonIoRuntimeExceptionExitsThreadWithoutRetrying() throws Exception {
        doThrow(new RuntimeException("Unsupported OS: test"))
                .when(ipcClient).connect();

        rpcThread = new DiscordRPCThread(ipcClient, 1L);
        rpcThread.enable();
        rpcThread.start();

        rpcThread.joinThread(2000L);

        assertThat(rpcThread.isThreadAlive()).isFalse();
        verify(ipcClient, times(1)).connect();
    }

    @Test
    @Tag("RealIpc")
    void realIpcClientStaysAliveOrConnectsGracefully() throws Exception {
        IPCClient realClient = new IPCClient(DISCORD_APP_CLIENT_ID);
        rpcThread = new DiscordRPCThread(realClient, 1L);
        rpcThread.enable();
        rpcThread.start();

        awaitTrue(() -> rpcThread.isConnected() || (!rpcThread.isAutoReconnect() && rpcThread.isWaiting()), 5000L);

        assertThat(rpcThread.isThreadAlive()).isTrue();
    }

    private static void awaitTrue(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() >= deadline) {
                throw new AssertionError("Condition was not met within " + timeoutMs + " ms");
            }
            Thread.sleep(10L);
        }
    }
}
