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
package org.terasology.testUtil;

import com.google.common.collect.Sets;
import org.terasology.config.flexible.SettingChangeListener;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.module.ClasspathModule;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataReader;
import org.terasology.naming.Name;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 */
public final class ModuleManagerFactory {
    private ModuleManagerFactory() {
    }

    public static ModuleManager create() throws Exception {
        ModuleManager moduleManager = new ModuleManagerImpl("");
        return loadModuleEnvironment(moduleManager);
    }

    /**
     * Creates a ModuleManager with a ModuleInstallManager instance configured to reference the masterServerAddress endpoint.
     * The endpoint is a URL where a registry of modules should be found. The ModuleInstallManager accesses the remote
     * modules via a ModuleListDownloader. The downloader pulls a JSON stream describing a collection of ModuleMetadata objects.
     * Each ModuleMetadata object describes a module available via the registry.
     *
     * @param masterServerAddress the url to a remote module registry
     * @return a ModuleManager containing an InstallationManager configured to access a remote module registry at URL masterServerAddress
     * @throws Exception if the ModuleManager cannot be loaded because engine metadata (engine-module.txt) cannot be found or
     * ModuleInstallManager is unable to resolve the masterServerAddress endpoint
     */
    public static ModuleManager create(String masterServerAddress) throws Exception {
        ModuleManager moduleManager = new ModuleManagerImpl(masterServerAddress);
        return loadModuleEnvironment(moduleManager);
    }

    private static ModuleManager loadModuleEnvironment(ModuleManager moduleManager) throws Exception {
        try (Reader reader = new InputStreamReader(ModuleManagerFactory.class.getResourceAsStream("/module.txt"), TerasologyConstants.CHARSET)) {
            ModuleMetadata metadata = new ModuleMetadataReader().read(reader);
            moduleManager.getRegistry().add(ClasspathModule.create(metadata, ModuleManagerFactory.class));
        }
        moduleManager.loadEnvironment(Sets.newHashSet(moduleManager.getRegistry().getLatestModuleVersion(new Name("engine"))), true);
        return moduleManager;
    }
}
