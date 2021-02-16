// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.generator.plugin;

import com.google.common.collect.Lists;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

import java.util.List;

/**
 */
public class DefaultWorldGeneratorPluginLibrary implements WorldGeneratorPluginLibrary {

    private final ClassLibrary<WorldGeneratorPlugin> library;

    public DefaultWorldGeneratorPluginLibrary(ModuleEnvironment moduleEnvironment, Context context) {
        library = new DefaultClassLibrary<>(moduleEnvironment, context.get(ReflectFactory.class), context.get(CopyStrategyLibrary.class));
        for (Class<?> entry : moduleEnvironment.getTypesAnnotatedWith(RegisterPlugin.class)) {
            if (WorldGeneratorPlugin.class.isAssignableFrom(entry)) {
                library.register(new ResourceUrn(moduleEnvironment.getModuleProviding(entry).toString(), entry.getSimpleName()), entry.asSubclass(WorldGeneratorPlugin.class));
            }
        }
    }

    @Override
    public <U extends WorldGeneratorPlugin> List<U> instantiateAllOfType(Class<U> ofType) {
        List<U> result = Lists.newArrayList();
        for (ClassMetadata<?, ?> classMetadata : library) {
            if (ofType.isAssignableFrom(classMetadata.getType()) && classMetadata.isConstructable() && classMetadata.getType().getAnnotation(RegisterPlugin.class) != null) {
                U item = ofType.cast(classMetadata.newInstance());
                if (item != null) {
                    result.add(item);
                }
            }
        }
        return result;
    }
}
