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

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModuleDownloadListGenerator {

    private ModuleRegistry localRegistry;
    private ModuleRegistry remoteRegistry;
    private DependencyResolver remoteDependencyResolver;

    ModuleDownloadListGenerator(ModuleRegistry localRegistry, ModuleRegistry remoteRegistry, DependencyResolver remoteDependencyResolver) {
        this.localRegistry = localRegistry;
        this.remoteRegistry = remoteRegistry;
        this.remoteDependencyResolver = remoteDependencyResolver;
    }

    public Set<Module> getAllModulesToDownloadFor(NameVersion moduleToInstall) throws DependencyResolutionFailedException {
        return getAllModulesToDownloadFor(moduleToInstall.getName(), moduleToInstall.getVersion());
    }

    public Set<Module> getAllModulesToDownloadFor(Name moduleName, Version moduleVersion) throws DependencyResolutionFailedException {
        return getAllModulesToDownloadFor(remoteRegistry.getModule(moduleName, moduleVersion));
    }

    public Set<Module> getAllModulesToDownloadFor(Name moduleName) throws DependencyResolutionFailedException {
        return getAllModulesToDownloadFor(remoteRegistry.getLatestModuleVersion(moduleName));
    }

    public Set<Module> getAllModulesToDownloadFor(Module moduleToInstall) throws DependencyResolutionFailedException {
        ResolutionResult result = remoteDependencyResolver.resolve(moduleToInstall.getMetadata().getId());
        if (!result.isSuccess()) {
            throw new DependencyResolutionFailedException("Module dependency resolution failed.");
        }
        Predicate<Module> isEngine = module -> module.getId().equals(TerasologyConstants.ENGINE_MODULE);
        Optional<Module> requiredEngine = result.getModules().stream().filter(isEngine).findFirst();
        Version currentEngineVersion = localRegistry.getLatestModuleVersion(TerasologyConstants.ENGINE_MODULE).getVersion();
        if (requiredEngine.isPresent() && !requiredEngine.get().getVersion().equals(currentEngineVersion)) {
            throw new DependencyResolutionFailedException("The required engine version does not match the current engine version.");
        }
        return result.getModules().stream()
                .filter(isEngine.negate())
                .filter(module -> isOnlineVersionNewer(localRegistry.getLatestModuleVersion(module.getId()), module))
                .collect(Collectors.toSet());
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
}
