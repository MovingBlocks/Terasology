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
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Runnable downloadTask = new Runnable() {
        @Override
        public void run() {
            try {
                status = "Downloading server list ..";

                @SuppressWarnings("serial")
                Type entryListType = new TypeToken<List<ServerInfo>>() { /**/ }.getType();
                URL url = new URL(serverAddress);

                try (Reader reader = new InputStreamReader(url.openStream(), cs)) {

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
            } catch (Exception e) {
                status = "Error: " + e.getLocalizedMessage();
                // we catch Exception here to make sure that it's being logged
                // alternative: re-throw as RuntimeException and use Thread.setUncaughtExceptionHandler()
                logger.error("Error downloading online server list!", e);
            }
        }
    };

    private final Queue<ServerInfo> servers = new ConcurrentLinkedQueue<>();

    private final Charset cs = StandardCharsets.UTF_8;
    private final String serverAddress;

    /**
     * "volatile" ensures the visibility of updates across different threads
     */
    private volatile String status;

    public ServerListDownloader(String serverAddress) {
        this.serverAddress = serverAddress;
        Thread dlThread = new Thread(downloadTask);
        dlThread.setName("ServerList Downloader");
        dlThread.start();
    }

    /**
     * @return a <b>thread-safe</b> list of servers
     */
    public Queue<ServerInfo> getServers() {
        return servers;
    }

    /**
     * @return
     */
    public String getStatus() {
        return status;
    }
}
