package org.terasology.config;

import org.terasology.game.TerasologyConstants;

/**
 * @author Immortius
 */
public class ServerInfo {
    private String name;
    private String address;
    private int port = TerasologyConstants.DEFAULT_PORT;

    public ServerInfo(String name, String address) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Server name must not be null or empty");
        }
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Server address must not be null or empty");
        }
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address != null && !address.isEmpty()) {
            this.address = address;
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
