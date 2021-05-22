// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import com.google.common.collect.Lists;

import org.terasology.engine.core.TerasologyConstants;

import java.util.Collections;
import java.util.List;

/**
 */
public class NetworkConfig {

    private List<ServerInfo> servers = Lists.newArrayList(new ServerInfo("localhost", "localhost", TerasologyConstants.DEFAULT_PORT));

    /**
     * Available upstream bandwidth in kilobits per second
     */
    private int upstreamBandwidth;

    /**
     * The port that is used for hosting
     */
    private int serverPort;

    /**
     * The server's Message of The Day
     */
    private String serverMOTD;

    /**
     * The master server URL
     */
    private String masterServer = "meta.terasology.org";

    public void clear() {
        servers.clear();
    }

    public int getUpstreamBandwidth() {
        return upstreamBandwidth;
    }

    public void setUpstreamBandwidth(int upstreamBandwidth) {
        this.upstreamBandwidth = upstreamBandwidth;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerMOTD() {
        return serverMOTD;
    }

    public void setServerMOTD(String serverMOTD) {
        this.serverMOTD = serverMOTD;
    }

    public void addServerInfo(ServerInfo serverInfo) {
        servers.add(serverInfo);
    }

    public void removeServerInfo(ServerInfo info) {
        servers.remove(info);
    }

    public List<ServerInfo> getServerInfos() {
        return Collections.unmodifiableList(servers);
    }

    public String getMasterServer() {
        return masterServer;
    }

    public void setMasterServer(String masterServer) {
        this.masterServer = masterServer;
    }
}
