// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModuleRegistry;

import java.util.Set;

/**
 * TODO Type description
 */
public interface ModuleManager {

    ModuleRegistry getRegistry();

    ModuleInstallManager getInstallManager();

    ModuleEnvironment getEnvironment();

    ModuleEnvironment loadEnvironment(Set<Module> modules, boolean asPrimary);

    ModuleMetadataJsonAdapter getModuleMetadataReader();

    ModuleFactory getModuleFactory();
}
