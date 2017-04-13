/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.network;

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
            sock.close();
            long millis = Duration.between(start, end).toMillis();
            return millis;
        }
    }
}

