// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

/**
 * A {@link Callable} that sends a ping to the specified host and returns the time in milliseconds.
 */
public class PingService implements Callable<Long> {

    private final String address;
    private final int port;
    private int timeout = 10000;

    public PingService(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * @return the ping time in milliseconds
     */
    @Override
    public Long call() throws IOException {
        Instant start = Instant.now();
        try (Socket sock = new Socket()) {
            InetSocketAddress endpoint = new InetSocketAddress(address, port);
            // One alternative is InetAddress.isReachable(), but it seems to require
            // root privileges under some operating systems
            sock.connect(endpoint, timeout);
            Instant end = Instant.now();
            return Duration.between(start, end).toMillis();
        }
    }
}

