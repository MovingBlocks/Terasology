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

import com.google.common.collect.Lists;
import org.terasology.engine.TerasologyConstants;

import java.util.List;
import java.util.Optional;

/**
 * @author Immortius
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
