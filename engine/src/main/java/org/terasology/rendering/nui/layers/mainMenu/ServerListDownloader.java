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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

/**
 * Downloads a list of servers from a given URL.
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

    private final Thread dlThread;

    public ServerListDownloader(String serverAddress) {
        this.serverAddress = serverAddress;
        dlThread = new Thread(this::download);
        dlThread.setName("ServerList Downloader");
        dlThread.start();
    }

    /**
     * @return a <b>thread-safe</b> list of servers
     */
    public List<ServerInfo> getServers() {
        return Collections.unmodifiableList(servers);
    }

    public boolean isDone() {
        return !dlThread.isAlive();
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
            status = "Error downloading server list!";
            // we catch Exception here to make sure that it's being logged
            // alternative: re-throw as RuntimeException and use
            // Thread.setUncaughtExceptionHandler()
            logger.error("Error downloading online server list!", e);
        }
    }

    private void download(String address) throws IOException {
        status = "Downloading server list ..";

        URL url = new URL("http", address, "/servers/list");
        try (Reader reader = new InputStreamReader(url.openStream(), charset);
                JsonReader jsonReader = new JsonReader(reader)) {

            status = "Parsing content ..";

            jsonReader.beginArray();

            TypeAdapter<ServerInfo> adapter = GSON.getAdapter(ServerInfo.class);

            while (jsonReader.hasNext()) {
                ServerInfo entry = adapter.read(jsonReader);
                servers.add(entry);

                logger.info("Retrieved game server {}", entry);

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // ignore - this is just to create an animation anyway
                }
            }

            jsonReader.endArray();

            status = String.format("Server list complete");
        }
    }
}
