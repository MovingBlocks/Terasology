// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.utilities.download.MultiFileDownloader;
import org.terasology.engine.utilities.download.MultiFileTransferProgressListener;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ModuleInstaller implements Callable<List<Module>> {

    private static final Logger logger = LoggerFactory.getLogger(ModuleInstaller.class);

    private Iterable<Module> moduleList;
    private ModuleManager moduleManager;
    private MultiFileTransferProgressListener downloadProgressListener;

    ModuleInstaller(ModuleManager moduleManager, Iterable<Module> moduleList,
                    MultiFileTransferProgressListener downloadProgressListener) {
        this.moduleList = moduleList;
        this.downloadProgressListener = downloadProgressListener;
        this.moduleManager = moduleManager;
    }

    @Override
    public List<Module> call() throws Exception {
        Map<URI, Path> filesToDownload = getDownloadUrls(moduleList);
        logger.info("Started downloading {} modules", filesToDownload.size()); //NOPMD
        MultiFileDownloader downloader = new MultiFileDownloader(filesToDownload, downloadProgressListener);
        List<Path> downloadedModulesPaths = downloader.call();
        logger.info("Module download completed, loading the new modules...");
        List<Module> newInstalledModules = new ArrayList<>(downloadedModulesPaths.size());
        for (Path filePath : downloadedModulesPaths) {
            try {
                Module module = moduleManager.registerArchiveModule(filePath);
                newInstalledModules.add(module);
            } catch (IOException e) {
                logger.warn("Could not load module {}", filePath.getFileName(), e); //NOPMD
            }
        }
        logger.info("Finished loading the downloaded modules");
        return newInstalledModules;
    }

    private Map<URI, Path> getDownloadUrls(Iterable<Module> modules) {
        Map<URI, Path> result = new HashMap<>();
        for (Module module : modules) {
            ModuleMetadata metadata = module.getMetadata();
            String version = metadata.getVersion().toString();
            String id = metadata.getId().toString();
            URI uri = null;
            try {
                uri = RemoteModuleExtension.getDownloadUrl(metadata).toURI();
            } catch (URISyntaxException e) {
                logger.error("Couldn't get download URL: ", e);
            }
            String fileName = String.format("%s-%s.jar", id, version);
            Path folder = PathManager.getInstance().getHomeModPath().normalize();
            Path target = folder.resolve(fileName);
            result.put(uri, target);
        }
        return result;
    }
}
