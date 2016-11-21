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
import org.terasology.internet.InternetManager;
import org.terasology.internet.TCPSocket;
import org.terasology.naming.Name;
import org.terasology.registry.DynamicInstanceProvider;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author soniex2
 */
public class InternetManagerImpl implements InternetManager, DynamicInstanceProvider<InternetManager> {
    /**
     * The semi-permanent instance cache.
     */
    private final Map<Name, InternetManager> permCache;
    private final InternetManager rootManager;
    private final Name moduleId;

    private static final TreeSet<String> allowedHosts; // TODO make this dynamic.

    static {
        allowedHosts = new TreeSet<>();
        allowedHosts.add("irc.freenode.net");
    }

    /**
     * Construct a new InternetManagerImpl with a fresh "permanent" cache.
     */
    public InternetManagerImpl() {
        permCache = Maps.newConcurrentMap();
        moduleId = null;
        rootManager = this;
    }

    /**
     * Construct a new InternetManagerImpl with a shared "permanent" cache.
     *
     * @param moduleId  The module ID.
     * @param permCache The "permanent" cache to share.
     */
    public InternetManagerImpl(Name moduleId, Map<Name, InternetManager> permCache, InternetManager rootManager) {
        this.permCache = permCache;
        this.moduleId = moduleId;
        this.rootManager = rootManager;
    }

    @Override
    public InternetManager getInstanceForModule(Name moduleId) {
        if (moduleId == null) {
            return this;
        }
        return permCache.computeIfAbsent(moduleId, x -> new InternetManagerImpl(x, permCache, rootManager));
    }

    @Override
    public TCPSocket openTCPConnection(String hostname, int port) throws IOException {
        if (!allowedHosts.contains(hostname)) {
            // TODO actual permission checking rather than just checking for the root manager.
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
