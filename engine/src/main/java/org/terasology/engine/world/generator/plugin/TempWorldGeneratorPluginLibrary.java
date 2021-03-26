// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator.plugin;

import org.terasology.engine.context.Context;
import org.terasology.module.ModuleEnvironment;

/**
 * A fake environment so that plugins can be loaded for configuration.
 *
 */
public class TempWorldGeneratorPluginLibrary extends DefaultWorldGeneratorPluginLibrary {

    public TempWorldGeneratorPluginLibrary(ModuleEnvironment environment, Context context) {
        super(environment, context);
    }
}
