// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.testUtil;

import com.google.common.collect.Sets;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.module.ModuleManagerImpl;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.naming.Name;

/**
 * Factory for creating {@link ModuleManager} with engine and unittest modules in registry. Also loads environment.
 */
public final class ModuleManagerFactory {
    private ModuleManagerFactory() {
    }

    /**
     * Create {@link ModuleManager} with engine and unittest module.
     *
     * @return Created {@link ModuleManager}.
     */
    public static ModuleManager create() {
        ModuleManager moduleManager = new ModuleManagerImpl("");
        Module unittestModule = moduleManager.getModuleFactory().createPackageModule("org.terasology.unittest");
        moduleManager.getRegistry().add(unittestModule);

        moduleManager.loadEnvironment(Sets.newHashSet(moduleManager.getRegistry().getLatestModuleVersion(new Name(
                "engine")), unittestModule), true);
        return moduleManager;
    }
}
