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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.paths.PathManager;
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
        logger.info("Started downloading {} modules", filesToDownload.size());
        MultiFileDownloader downloader = new MultiFileDownloader(filesToDownload, downloadProgressListener);
        List<Path> downloadedModulesPaths = downloader.call();
        logger.info("Module download completed, loading the new modules...");
        List<Module> newInstalledModules = new ArrayList<>(downloadedModulesPaths.size());
//        ModuleLoader loader = new ModuleLoader(moduleManager.getModuleMetadataReader());
//        loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);
        for (Path filePath : downloadedModulesPaths) {
            try {
                Module module = moduleManager.getModuleFactory().createModule(filePath.toFile());
                moduleManager.getRegistry().add(module);
                newInstalledModules.add(module);
            } catch (IOException e) {
                logger.warn("Could not load module {}", filePath.getFileName(), e);
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
                e.printStackTrace();
            }
            String fileName = String.format("%s-%s.jar", id, version);
            Path folder = PathManager.getInstance().getHomeModPath().normalize();
            Path target = folder.resolve(fileName);
            result.put(uri, target);
        }
        return result;
    }
}
