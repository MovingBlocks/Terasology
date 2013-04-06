/*
 * Copyright 2013 Moving Blocks
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

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class NetworkConfig implements Iterable<ServerInfo> {
    private List<ServerInfo> servers = Lists.newArrayList();
    // Available upstream bandwidth in kilobits per second
    private int upstreamBandwidth = 1024;

    public void clear() {
        servers.clear();
    }

    public int getUpstreamBandwidth() {
        return upstreamBandwidth;
    }

    public void setUpstreamBandwidth(int upstreamBandwidth) {
        this.upstreamBandwidth = upstreamBandwidth;
    }

    @Override
    public Iterator<ServerInfo> iterator() {
        return servers.iterator();
    }

    public void add(ServerInfo serverInfo) {
        servers.add(serverInfo);
    }

    public void remove(ServerInfo info) {
        servers.remove(info);
    }
}
