/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.socket.internal;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.engine.GameThread;
import org.terasology.exception.SandboxException;
import org.terasology.naming.Name;
import org.terasology.socket.SocketAccess;
import org.terasology.socket.TCPSocket;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public class SocketAccessImpl implements SocketAccess {
    private final Map<String, TIntSet> allowedHosts;

    public SocketAccessImpl(Name moduleId) {
        allowedHosts = new HashMap<>();
    }

    @Override
    public TCPSocket openTCPConnection(String hostname, int port) throws SandboxException, IOException {
        if (!GameThread.isCurrentThread()) {
            throw new IllegalStateException("Wrong thread");
        }
        if (!(this.allowedHosts.containsKey(hostname) || this.allowedHosts.containsKey("*"))) {
            throw new SandboxException("Blocked hostname");
        }

        TIntSet portSet = this.allowedHosts.getOrDefault(hostname, this.allowedHosts.get("*"));
        boolean isBlacklist = portSet.contains(-1);
        boolean portInSet = portSet.contains(port);
        if (isBlacklist) {
            if (portInSet) {
                throw new SandboxException("Blocked port");
            }
        } else {
            if (!portInSet) {
                throw new SandboxException("Blocked port");
            }
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
