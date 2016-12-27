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
package org.terasology.socket.internal;

import com.google.common.collect.Maps;
import gnu.trove.TCollections;
import gnu.trove.set.TIntSet;
import org.terasology.config.Config;
import org.terasology.config.SocketConfig;
import org.terasology.context.Context;
import org.terasology.engine.GameThread;
import org.terasology.exception.SandboxException;
import org.terasology.socket.SocketManager;
import org.terasology.socket.TCPSocket;
import org.terasology.naming.Name;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Map;

/**
 * Implements SocketManager.
 */
public class SocketManagerImpl implements SocketManager {
    /**
     * The allowed hosts.
     */
    //@Nonnull
    private final Map<String, TIntSet> allowedHosts;

    /**
     * Construct a new SocketManagerImpl with a shared "permanent" cache.
     *
     * @param moduleId The module ID.
     * @param context  The current context.
     */
    public SocketManagerImpl(Name moduleId, Context context) {
        if (!GameThread.isCurrentThread()) {
            throw new IllegalStateException("Wrong thread");
        }
        Config theConfig = context.get(Config.class);
        if (theConfig == null) {
            throw new IllegalStateException();
        }
        // TODO check if this could miss config updates, and make thread safe.
        SocketConfig.Hosts hosts = theConfig.getSocket().getTcpWhitelists().get(moduleId);
        if (hosts == null) {
            allowedHosts = Collections.emptyMap();
            return;
        }
        allowedHosts = Maps.transformValues(hosts.getHosts(), (x) -> x != null ? TCollections.unmodifiableSet(x.getPorts()) : null);
    }

    @Override
    public TCPSocket openTCPConnection(String hostname, int port) throws IOException, SandboxException {
        if (!GameThread.isCurrentThread()) {
            throw new IllegalStateException("Wrong thread");
        }
        if (!(this.allowedHosts.containsKey(hostname) || this.allowedHosts.containsKey("*"))) {
            throw new SandboxException(new IllegalArgumentException("Blocked hostname"));
        }

        TIntSet portSet = this.allowedHosts.getOrDefault(hostname, this.allowedHosts.get("*"));
        boolean isBlacklist = portSet.contains(-1);
        boolean portInSet = portSet.contains(port);
        if (isBlacklist) {
            if (portInSet) {
                throw new SandboxException(new IllegalArgumentException("Blocked port"));
            }
        } else {
            if (!portInSet) {
                throw new SandboxException(new IllegalArgumentException("Blocked port"));
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
