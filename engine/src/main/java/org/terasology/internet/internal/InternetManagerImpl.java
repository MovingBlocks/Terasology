/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.internet.internal;

import org.terasology.internet.InternetManager;
import org.terasology.internet.TCPSocket;
import org.terasology.naming.Name;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements InternetManager.
 */
public class InternetManagerImpl implements InternetManager {
    private final Name moduleId;

    private final Set<String> allowedHosts;

    /**
     * Construct a new InternetManagerImpl with a shared "permanent" cache.
     *
     * @param moduleId The module ID.
     */
    public InternetManagerImpl(Name moduleId) {
        this.moduleId = moduleId;
        allowedHosts = new TreeSet<>();
        allowedHosts.add("irc.freenode.net");
    }

    @Override
    public TCPSocket openTCPConnection(String hostname, int port) throws IOException {
        if (!allowedHosts.contains(hostname)) {
            throw new IllegalArgumentException();
        }
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<TCPSocket>) () -> {
                try {
                    Socket socket = new Socket(hostname, port);
                    return new TCPSocketImpl(socket);
                } catch (MalformedURLException | UnknownHostException e) {
                    throw new IOException(e);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
    }
}
