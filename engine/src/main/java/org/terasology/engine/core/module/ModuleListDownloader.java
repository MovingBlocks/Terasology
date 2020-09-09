// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Downloads module meta-info from a given URL.
 */
public class ModuleListDownloader implements Callable<ModuleRegistry> {

    private static final Logger logger = LoggerFactory.getLogger(ModuleListDownloader.class);

    private final ModuleMetadataJsonAdapter metaReader = new ModuleMetadataJsonAdapter();

    private final String serverAddress;

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
        //FIXME: reimplement we haven't BaseModule now - gestalt v7
//        try (JsonReader reader = new JsonReader(new InputStreamReader(url.openStream(), TerasologyConstants
//        .CHARSET))) {
//
//            logger.info("Parsing content ..");
//
//            reader.beginArray();
//
//            while (reader.hasNext()) {
//                ModuleMetadata meta = metaReader.read(reader);
//                logger.debug("Read module {} - {}", meta.getId(), meta.getVersion());
//                RemoteModule remoteModule = new RemoteModule(meta);
//                modules.add(remoteModule);
//            }
//
//            reader.endArray();
//
//            int count = modules.size();
//            logger.info(String.format("Retrieved %d %s", count, (count == 1) ? "entry" : "entries"));
//        }
        return modules;
    }
}
