// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.generator.plugin;

import org.terasology.context.Context;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 * A fake environment so that plugins can be loaded for configuration.
 *
 */
public class TempWorldGeneratorPluginLibrary extends DefaultWorldGeneratorPluginLibrary {

    public TempWorldGeneratorPluginLibrary(ModuleEnvironment environment, Context context) {
        super(environment, context.get(ReflectFactory.class), context.get(CopyStrategyLibrary.class));
    }
}
