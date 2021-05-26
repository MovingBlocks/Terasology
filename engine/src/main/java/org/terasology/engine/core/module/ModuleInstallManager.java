// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
