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

import org.terasology.engine.utilities.download.MultiFileTransferProgressListener;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.naming.Name;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

public class ModuleInstallManager {

    private ModuleManager moduleManager;
    private ModuleRegistry remoteRegistry;
    private ModuleListDownloader remoteRegistryUpdater;
    private ModuleDownloadListGenerator downloadListGenerator;

    ModuleInstallManager(ModuleManager moduleManager, String remoteMasterServerAddress) {
        this.moduleManager = moduleManager;
        this.remoteRegistryUpdater = new ModuleListDownloader(remoteMasterServerAddress);
    }

    public Callable<Void> updateRemoteRegistry() {
        return (() -> {
            remoteRegistry = remoteRegistryUpdater.call();
            downloadListGenerator = new ModuleDownloadListGenerator(moduleManager.getRegistry(), new DependencyResolver(remoteRegistry));
            return null;
        });
    }

    public Collection<Module> getRemoteRegistry() {
        return Collections.unmodifiableCollection(remoteRegistry);
    }

    public Set<Module> getAllModulesToDownloadFor(Name... modulesToInstall) throws DependencyResolutionFailedException {
        if (downloadListGenerator == null) {
            throw new DependencyResolutionFailedException("The online module list hasn't been downloaded.");
        }
        return downloadListGenerator.getAllModulesToDownloadFor(modulesToInstall);
    }

    public ModuleInstaller createInstaller(Iterable<Module> modules, MultiFileTransferProgressListener progressListener) {
        return new ModuleInstaller(moduleManager,  modules, progressListener);
    }
}
