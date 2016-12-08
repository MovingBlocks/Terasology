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

import com.google.common.collect.Maps;
import gnu.trove.TCollections;
import gnu.trove.set.TIntSet;
import org.terasology.config.Config;
import org.terasology.config.InternetConfig;
import org.terasology.context.Context;
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
import java.util.Collections;
import java.util.Map;

/**
 * Implements InternetManager.
 */
public class InternetManagerImpl implements InternetManager {
    /**
     * The allowed hosts.
     */
    //@Nonnull
    private final Map<String, TIntSet> allowedHosts;

    /**
     * Construct a new InternetManagerImpl with a shared "permanent" cache.
     *
     * @param moduleId The module ID.
     * @param context  The current context.
     */
    public InternetManagerImpl(Name moduleId, Context context) {
        Config theConfig = context.get(Config.class);
        if (theConfig == null) {
            throw new IllegalStateException();
        }
        // TODO check if this could miss config updates, and make thread safe.
        InternetConfig.Hosts hosts = theConfig.getInternet().getTcpWhitelists().get(moduleId);
        if (hosts == null) {
            allowedHosts = Collections.emptyMap();
            return;
        }
        allowedHosts = Maps.transformValues(hosts.getHosts(), (x) -> x != null ? TCollections.unmodifiableSet(x.getPorts()) : null);
    }

    @Override
    public TCPSocket openTCPConnection(String hostname, int port) throws IOException {
        if (!(this.allowedHosts.containsKey(hostname) || this.allowedHosts.containsKey("*"))) {
            // neither hostname nor wildcard in allowedHosts
            // TODO maybe use a different exception type? (SandboxException or something?)
            throw new IllegalArgumentException("Blocked hostname");
        }
        // TODO We have many options when both a wildcard entry and a specific entry exist. Which one should we pick?
        // 1. Merge allowed ports.
        // 2. Wildcard overrides specific hostname.
        // 3. Specific hostname overrides wildcard. This option pleases me the most. (It also enables blacklists.)
        TIntSet allowedPorts = this.allowedHosts.getOrDefault(hostname, this.allowedHosts.get("*"));
        if (!(allowedPorts.contains(port) || allowedPorts.contains(-1))) {
            // neither port nor wildcard in allowedPorts
            // TODO maybe use a different exception type? (SandboxException or something?)
            throw new IllegalArgumentException("Blocked port");
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
