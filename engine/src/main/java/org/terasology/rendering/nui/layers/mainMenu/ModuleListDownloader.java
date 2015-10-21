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

import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.NetworkConfig;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.RemoteModuleExtension;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Downloads module meta-info from a given URL.
 */
class ModuleListDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListDownloader.class);

    private final List<RemoteModule> modules = new CopyOnWriteArrayList<>();

    private final ModuleMetadataJsonAdapter metaReader = new ModuleMetadataJsonAdapter();

    private final String serverAddress;

    /**
     * "volatile" ensures the visibility of updates across different threads
     */
    private volatile String status;

    private Thread dlThread;

    public ModuleListDownloader(String serverAddress) {
        this.serverAddress = serverAddress;
        dlThread = new Thread(this::download);
        dlThread.setName("ModuleList Downloader");
        dlThread.start();

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metaReader.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    /**
     * @return a <b>thread-safe</b> list of servers
     */
    public List<RemoteModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /**
     * @return the current status
     */
    public String getStatusText() {
        return status;
    }

    public boolean isDone() {
        return !dlThread.isAlive();
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
        status = "Downloading modules ..";

        URL url = new URL("http", address, "/modules/list/latest");
        try (JsonReader reader = new JsonReader(new InputStreamReader(url.openStream(), TerasologyConstants.CHARSET))) {

            status = "Parsing content ..";

            reader.beginArray();

            while (reader.hasNext()) {
                ModuleMetadata meta = metaReader.read(reader);
                logger.debug("Read module {} - {}", meta.getId(), meta.getVersion());
                RemoteModule remoteModule = new RemoteModule(meta);
                modules.add(remoteModule);

                int count = modules.size();
                status = String.format("Retrieved %d %s", count, (count == 1) ? "entry" : "entries");
            }

            reader.endArray();
        }
    }
}
