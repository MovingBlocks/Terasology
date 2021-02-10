// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.layers.mainMenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.NetworkConfig;
import org.terasology.config.ServerInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
     * The i18n key corresponding to the current status of the downloader
     *
     * "volatile" ensures the visibility of updates across different threads
     */
    private volatile String status;

    private final Thread dlThread;

     ServerListDownloader(String serverAddress) {
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
     * @return the i18n key corresponding to the current status of the downloader
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
            status = "${engine:menu#error-downloading-server-list}";
            // we catch Exception here to make sure that it's being logged
            // alternative: re-throw as RuntimeException and use
            // Thread.setUncaughtExceptionHandler()
            logger.error("Error downloading online server list!", e);
        }
    }

    private void download(String address) throws IOException {
        status = "${engine:menu#downloading-server-list}";

        URL url = new URL("http", address, "/servers/list");
        try (Reader reader = new InputStreamReader(url.openStream(), charset);
                JsonReader jsonReader = new JsonReader(reader)) {

            status = "${engine:menu#parsing-content}";

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
            
            if(servers.size() == 0) {
                status = String.format("Server Error!");
            }
            else {
                status = String.format("${engine:menu#server-list-complete}");
            }
        }
    }
}
