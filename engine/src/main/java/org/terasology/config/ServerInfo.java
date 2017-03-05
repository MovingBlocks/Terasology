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
package org.terasology.config;

import com.google.common.base.Preconditions;

public class ServerInfo {
    private String name;
    private String address;
    private String owner;
    private int port;
    private boolean active = true;

    ServerInfo() {
        // for serialization purposes
    }


    /**
     * ServerInfo constructor.
     *
     * @param name    Sets the name of the server.
     * @param address Sets the address (URL) of the server.
     * @param port    Sets the port of the server.
     */
    public ServerInfo(String name, String address, int port) {
        setName(name);
        setAddress(address);
        setPort(port);
    }

    /**
     * @return Returns the name of the server
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Sets the name of the server
     */
    public void setName(String name) {
        Preconditions.checkArgument(name != null && !name.isEmpty(), "Server name must not be null or empty");
        this.name = name;
    }

    /**
     * @return Returns the address (URL) of the server
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address Sets the address
     */
    public void setAddress(String address) {
        Preconditions.checkArgument(address != null && !address.isEmpty(), "Server address must not be null or empty");
        this.address = address;
    }

    /**
     * @return Returns the port an integer value representing the port number, in the [0-65535] range, inclusive
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port Sets the port, ranges from 0 to 65535 inclusive
     */
    public void setPort(int port) {
        Preconditions.checkArgument(port >= 0 && port <= 65535, "Server port must be in the range [0..65535]");
        this.port = port;
    }

    /**
     * @return Returns the server owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner Sets the server owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return Returns if the server is active or not
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active Sets if the server is active or not
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return A String object containing all of the variables of your object
     */
    @Override
    public String toString() {
        return "ServerInfo [name=" + name + ", address=" + address + ", port=" + port +
                ", owner=" + owner + ", active=" + active + "]";
    }
}
