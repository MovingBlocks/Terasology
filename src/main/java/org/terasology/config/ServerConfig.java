package org.terasology.config;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class ServerConfig implements Iterable<ServerInfo> {
    private List<ServerInfo> servers = Lists.newArrayList();

    public void clear() {
        servers.clear();
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
