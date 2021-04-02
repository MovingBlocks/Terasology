// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.testUtil;

import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.Module;

public final class ModuleManagerFactory {

    private ModuleManagerFactory() {
    }

    public static ModuleManager create() throws Exception {
        ModuleManager moduleManager = new ModuleManager("");
        Module unittestModule = moduleManager.registerPackageModule("org.terasology.unittest");
        moduleManager.resolveAndLoadEnvironment(unittestModule.getId());
        return moduleManager;
    }
}
