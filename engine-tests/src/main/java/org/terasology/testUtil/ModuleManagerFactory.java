// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.testUtil;

import com.google.common.collect.Sets;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.module.ModuleManagerImpl;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.naming.Name;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 */
public final class ModuleManagerFactory {
    private ModuleManagerFactory() {
    }

    public static ModuleManager create() throws Exception {
        ModuleManager moduleManager = new ModuleManagerImpl("");
        try (Reader reader = new InputStreamReader(ModuleManagerFactory.class.getResourceAsStream("/module.txt"),
                TerasologyConstants.CHARSET)) {
            ModuleMetadata metadata = new ModuleMetadataJsonAdapter().read(reader);
            Module unittestModule = moduleManager.getModuleFactory().createModule(metadata,
                    new File(ModuleManagerFactory.class.getResource("/").getFile())); // some hack to load current
            // directory. TODO: moving to package module
            moduleManager.getRegistry().add(unittestModule);
        }
        moduleManager.loadEnvironment(Sets.newHashSet(moduleManager.getRegistry().getLatestModuleVersion(new Name(
                "engine"))), true);
        return moduleManager;
    }
}
