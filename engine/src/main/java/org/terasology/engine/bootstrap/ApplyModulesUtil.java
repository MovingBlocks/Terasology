/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.classMetadata.copying.CopyStrategy;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.module.ModuleManager;
import org.terasology.utilities.ReflectionUtil;

import java.util.Set;

/**
 * @author Immortius
 */
public final class ApplyModulesUtil {
    private static final Logger logger = LoggerFactory.getLogger(ApplyModulesUtil.class);

    private ApplyModulesUtil() {
    }

    public static void applyModules() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        moduleManager.applyActiveModules();

        CopyStrategyLibrary copyStrategyLibrary = CoreRegistry.get(CopyStrategyLibrary.class);
        copyStrategyLibrary.clear();
        Set<Class<? extends CopyStrategy>> copyStrategies = moduleManager.getActiveModuleReflections().getSubTypesOf(CopyStrategy.class);
        for (Class<? extends CopyStrategy> copyStrategy : copyStrategies) {
            Class targetType = ReflectionUtil.getTypeParameterForSuper(copyStrategy, CopyStrategy.class, 0);
            if (targetType != null) {
                try {
                    copyStrategyLibrary.register(targetType, copyStrategy.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Cannot register CopyStrategy '{}' - failed to instantiate", copyStrategy, e);
                }
            } else {
                logger.error("Cannot register CopyStrategy '{}' - unable to determine target type", copyStrategy);
            }
        }

        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        assetManager.clear();
        assetManager.applyOverrides();


    }
}
