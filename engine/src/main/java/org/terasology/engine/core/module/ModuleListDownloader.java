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

package org.terasology.engine.core.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.resources.EmptyFileSource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * Downloads module meta-info from a given URL.
 */
public class ModuleListDownloader implements Callable<ModuleRegistry> {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListDownloader.class);

    private final ModuleMetadataJsonAdapter metaReader = new ModuleMetadataJsonAdapter();

    private final String serverAddress;
    private final Gson gson = new Gson();

    ModuleListDownloader(String serverAddress) {
        this.serverAddress = serverAddress;

        for (RemoteModuleExtension ext : RemoteModuleExtension.values()) {
            metaReader.registerExtension(ext.getKey(), ext.getValueType());
        }
    }

    @Override
    public ModuleRegistry call() throws IOException {
        logger.info("Downloading modules ..");

        TableModuleRegistry modules = new TableModuleRegistry();
        URL url = new URL("http", serverAddress, "/modules/list/latest");
        try (InputStreamReader reader = new InputStreamReader(url.openStream(), TerasologyConstants.CHARSET)) {

            logger.info("Parsing content ..");

            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            for (JsonElement jObject : jsonArray) {
                String json = gson.toJson(jObject);

                ModuleMetadata meta = metaReader.read(new StringReader(json));
                logger.debug("Read module {} - {}", meta.getId(), meta.getVersion());
                modules.add(new Module(meta, new EmptyFileSource(), Collections.emptyList(), new Reflections(),
                        (c) -> false));
            }

            int count = modules.size();
            logger.info(String.format("Retrieved %d %s", count, (count == 1) ? "entry" : "entries"));
        }
        return modules;
    }
}
