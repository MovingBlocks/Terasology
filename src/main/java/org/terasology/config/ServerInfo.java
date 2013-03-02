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
