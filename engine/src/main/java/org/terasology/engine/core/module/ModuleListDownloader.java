// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.gestalt.di.index.CompoundClassIndex;
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
                logger.debug("Read module {} - {}", meta.getId(), meta.getVersion()); //NOPMD
                modules.add(new Module(meta, new EmptyFileSource(), Collections.emptyList(), new CompoundClassIndex(),
                        (c) -> false));
            }

            int count = modules.size();
            logger.info("Retrieved {} {}", count, (count == 1) ? "entry" : "entries"); //NOPMD
        }
        return modules;
    }
}
