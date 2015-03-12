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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.ServerInfo;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.xerces.internal.util.URI;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class OnlineServerListBinding extends ReadOnlyBinding<List<ServerInfo>> {

    private static final Logger logger = LoggerFactory.getLogger(OnlineServerListBinding.class);

    private final List<ServerInfo> servers;

    /**
     * @param locals
     */
    public OnlineServerListBinding(List<ServerInfo> locals) {
        this.servers = Lists.newCopyOnWriteArrayList(locals);

        Thread dlThread = new Thread(new Downloader());
        dlThread.setName("ServerList Downloader");
        dlThread.start();
    }

    @Override
    public List<ServerInfo> get() {
        return servers;
    }

    private class Downloader implements Runnable {
        @Override
        public void run() {
            try {
                URL url = new URL("http://master-server.herokuapp.com/servers/list");
                Charset cs = StandardCharsets.UTF_8;
                Gson gson = new GsonBuilder().create();

                @SuppressWarnings("serial")
                Type entryListType = new TypeToken<List<ServerInfo>>() { /**/ }.getType();

                try (Reader reader = new InputStreamReader(url.openStream(), cs)) {
                    List<ServerInfo> onlineServers = gson.fromJson(reader, entryListType);
                    for (ServerInfo entry : onlineServers) {
                        logger.debug("Retrieved online game server {}", entry);
                    }
                    servers.addAll(onlineServers);
                }
            } catch (Exception e) {
                // we catch Exception here to make sure that it's being logged
                logger.error("Error downloading online server list!", e);
            }
        }
    }
}
