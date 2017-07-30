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
package org.terasology.engine.module;

import org.terasology.engine.TerasologyConstants;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;
import org.terasology.naming.Version;
import org.terasology.utilities.download.MultiFileTransferProgressListener;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModuleInstallManager {

    private ModuleManager moduleManager;
    private ModuleRegistry remoteRegistry;
    private ModuleListDownloader remoteRegistryUpdater;

    ModuleInstallManager(ModuleManager moduleManager, String remoteMasterServerAddress) {
        this.moduleManager = moduleManager;
        this.remoteRegistryUpdater = new ModuleListDownloader(remoteMasterServerAddress);
    }

    public Callable<Void> updateRemoteRegistry() {
        return (() -> {
            remoteRegistry = remoteRegistryUpdater.call();
            return null;
        });
    }

    public Collection<Module> getRemoteRegistry() {
        return Collections.unmodifiableCollection(remoteRegistry);
    }

    private void checkRemoteRegistry() throws DependencyResolutionFailedException {
        if (remoteRegistry == null) {
            throw new DependencyResolutionFailedException("The online module list hasn't been downloaded");
        }
    }

    public List<Module> getDependentModulesToDownload(NameVersion moduleToInstall) throws DependencyResolutionFailedException {
        return getDependentModulesToDownload(moduleToInstall.getName(), moduleToInstall.getVersion());
    }

    public List<Module> getDependentModulesToDownload(Name moduleName, Version moduleVersion) throws DependencyResolutionFailedException {
        checkRemoteRegistry();
        return getDependentModulesToDownload(remoteRegistry.getModule(moduleName, moduleVersion));
    }

    public List<Module> getDependentModulesToDownload(Name moduleName) throws DependencyResolutionFailedException {
        checkRemoteRegistry();
        return getDependentModulesToDownload(remoteRegistry.getLatestModuleVersion(moduleName));
    }

    public List<Module> getDependentModulesToDownload(Module moduleToInstall) throws DependencyResolutionFailedException {
        checkRemoteRegistry();
        ModuleRegistry localRegistry = moduleManager.getRegistry();
        DependencyResolver remoteDependencyResolver = new DependencyResolver(remoteRegistry);
        ResolutionResult result = remoteDependencyResolver.resolve(moduleToInstall.getMetadata().getId());
        if (!result.isSuccess()) {
            throw new DependencyResolutionFailedException("Module dependency resolution failed.");
        }
        Predicate<Module> isEngine = module -> module.getId().equals(TerasologyConstants.ENGINE_MODULE);
        Optional<Module> requiredEngine = result.getModules().stream().filter(isEngine).findFirst();
        Version currentEngineVersion = moduleManager.getRegistry().getLatestModuleVersion(TerasologyConstants.ENGINE_MODULE).getVersion();
        if (requiredEngine.isPresent() && !requiredEngine.get().getVersion().equals(currentEngineVersion)) {
            throw new DependencyResolutionFailedException("The required engine version does not match the current engine version.");
        }
        return result.getModules().stream()
                .filter(isEngine.negate())
                .filter(module -> isOnlineVersionNewer(localRegistry.getLatestModuleVersion(module.getId()), module))
                .collect(Collectors.toList());
    }

    private boolean isOnlineVersionNewer(Module localVersion, Module onlineVersion) {
        if (onlineVersion == null) {
            return false;
        }
        if (localVersion == null) {
            return true;
        }
        int versionCompare = onlineVersion.getVersion().compareTo(localVersion.getVersion());
        if (versionCompare > 0) {
            return true;
        } else if (versionCompare == 0) {
                /*
                 * Multiple binaries get released as the same snapshot version, A version name match thus does not
                 * gurantee that we have the newest version already if it is a snapshot version.
                 *
                 * Having the user redownload the same binary again is not ideal, but it is better then having the user
                 * being stuck on an outdated snapshot binary.
                 */
            return onlineVersion.getVersion().isSnapshot();
        } else {
            return false;
        }
    }

    public ModuleInstaller createInstaller(Iterable<Module> modules, MultiFileTransferProgressListener progressListener) {
        return new ModuleInstaller(moduleManager,  modules, progressListener);
    }
}
