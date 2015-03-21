/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.config;

import java.util.List;

import org.terasology.engine.TerasologyConstants;

import com.google.common.collect.Lists;

/**
 * @author Immortius
 */
public class NetworkConfig {

    private List<ServerInfo> servers = Lists.newArrayList(new ServerInfo("localhost", "localhost", 25777));

    /**
     * Available upstream bandwidth in kilobits per second
     */
    private int upstreamBandwidth = 1024;

    /**
     * The port that is used for hosting
     */
    private int serverPort = TerasologyConstants.DEFAULT_PORT;

    /**
     * The master server URL
     */
    private String masterServer = "https://master-server.herokuapp.com/servers/list";

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

    public void add(ServerInfo serverInfo) {
        servers.add(serverInfo);
    }

    public void remove(ServerInfo info) {
        servers.remove(info);
    }

    public List<ServerInfo> getServers() {
        return servers;
    }

    public void setServers(List<ServerInfo> servers) {
        this.servers = servers;
    }

    public String getMasterServer() {
        return masterServer;
    }

    public void setMasterServer(String masterServer) {
        this.masterServer = masterServer;
    }
}
