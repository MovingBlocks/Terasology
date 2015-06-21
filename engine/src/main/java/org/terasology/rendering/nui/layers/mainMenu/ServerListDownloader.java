/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui.layers.mainMenu;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.NetworkConfig;
import org.terasology.config.ServerInfo;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Downloads a list of servers from a given URL.
 * @author Martin Steiger
 */
class ServerListDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ServerListDownloader.class);

    private static final Gson GSON = new GsonBuilder().create();

    private final List<ServerInfo> servers = new CopyOnWriteArrayList<>();

    private final Charset charset = StandardCharsets.UTF_8;
    private final String serverAddress;

    /**
     * "volatile" ensures the visibility of updates across different threads
     */
    private volatile String status;

    public ServerListDownloader(String serverAddress) {
        this.serverAddress = serverAddress;
        Thread dlThread = new Thread(this::download);
        dlThread.setName("ServerList Downloader");
        dlThread.start();
    }

    /**
     * @return a <b>thread-safe</b> list of servers
     */
    public List<ServerInfo> getServers() {
        return Collections.unmodifiableList(servers);
    }

    /**
     * @return the current status
     */
    public String getStatus() {
        return status;
    }

    // this is run on a parallel thread
    private void download() {
        try {
            try {
                download(serverAddress);
            } catch (Exception e) {
                String defaultAddress = new NetworkConfig().getMasterServer();
                if (!defaultAddress.equals(serverAddress)) {
                    logger.warn("Download server list from {} failed. Trying default ..", serverAddress);
                    download(defaultAddress);
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            status = "Error: " + e.toString();
            // we catch Exception here to make sure that it's being logged
            // alternative: re-throw as RuntimeException and use
            // Thread.setUncaughtExceptionHandler()
            logger.error("Error downloading online server list!", e.toString());
        }
    }

    private void download(String address) throws IOException {
        status = "Downloading server list ..";

        @SuppressWarnings("serial")
        Type entryListType = new TypeToken<List<ServerInfo>>() { /**/ }.getType();

        URL url = new URL("http", address, "/servers/list");
        try (Reader reader = new InputStreamReader(url.openStream(), charset)) {

            status = "Parsing content ..";

            List<ServerInfo> onlineServers = GSON.fromJson(reader, entryListType);
            if (onlineServers == null) {
                throw new IOException("Invalid server list file content!");
            }

            for (ServerInfo entry : onlineServers) {
                logger.debug("Retrieved online game server {}", entry);
            }

            int count = onlineServers.size();
            status = String.format("Retrieved %d %s", count, (count == 1) ? "entry" : "entries");

            servers.addAll(onlineServers);
        }
    }
}
