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

import org.terasology.engine.core.TerasologyConstants;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.PathModule;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.naming.Version;

import java.util.Set;
import java.util.stream.Collectors;

class ModuleDownloadListGenerator {

    private ModuleRegistry localRegistry;
    private DependencyResolver remoteDependencyResolver;

    ModuleDownloadListGenerator(ModuleRegistry localRegistry, DependencyResolver remoteDependencyResolver) {
        this.localRegistry = localRegistry;
        this.remoteDependencyResolver = remoteDependencyResolver;
    }

    Set<Module> getAllModulesToDownloadFor(Name... modulesToInstall) throws DependencyResolutionFailedException {
        Version currentEngineVersion = localRegistry.getLatestModuleVersion(TerasologyConstants.ENGINE_MODULE).getVersion();
        ResolutionResult resolutionResult = remoteDependencyResolver.builder()
                .requireVersion(TerasologyConstants.ENGINE_MODULE, currentEngineVersion)
                .requireAll(modulesToInstall)
                .build();
        return processResolutionResult(resolutionResult);
    }

    private Set<Module> processResolutionResult(ResolutionResult resolutionResult) throws DependencyResolutionFailedException {
        if (!resolutionResult.isSuccess()) {
            throw new DependencyResolutionFailedException("Module dependency resolution failed.");
        }
        return resolutionResult.getModules().stream()
                .filter(module -> !module.getId().equals(TerasologyConstants.ENGINE_MODULE))
                .filter(module -> isOnlineVersionNewer(localRegistry.getLatestModuleVersion(module.getId()), module))
                .collect(Collectors.toSet());
    }

    /**
     * Whether the given module is present as source in the workspace.
     *
     * This is relevant when running the game from a local workspace to prevent downloading modules already present as
     * source.
     */
    private boolean isSourceModule(Module module) {
        return module instanceof PathModule;
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
            if (isSourceModule(localVersion)) {
                // In case the version of the local source module is the same as remote, don't download it again.
                return false;
            } else {
                /*
                 * Multiple binaries get released as the same snapshot version, A version name match thus does not
                 * guarantee that we have the newest version already if it is a snapshot version.
                 *
                 * Having the user redownload the same binary again is not ideal, but it is better then having the user
                 * being stuck on an outdated snapshot binary.
                 */
                return onlineVersion.getVersion().isSnapshot();
            }
        } else {
            return false;
        }
    }
}
