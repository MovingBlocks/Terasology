// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.testUtil;

import com.google.common.collect.ImmutableList;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.Module;
import org.terasology.nui.UIWidget;

public final class ModuleManagerFactory {

    private ModuleManagerFactory() {
    }

    public static ModuleManager create() {
        // Loading screens, among other things, break when NUI classes are not added to engine.
        ModuleManager moduleManager = new ModuleManager("", ImmutableList.of(UIWidget.class));
        Module unittestModule = moduleManager.registerPackageModule("org.terasology.unittest");
        moduleManager.resolveAndLoadEnvironment(unittestModule.getId());
        return moduleManager;
    }
}
