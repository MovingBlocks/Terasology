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
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.copy.RegisterCopyStrategy;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.ReflectionUtil;
import org.terasology.world.block.family.BlockFamilyFactory;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.RegisterBlockFamilyFactory;

/**
 * @author Immortius
 */
public final class ApplyModulesUtil {
    private static final Logger logger = LoggerFactory.getLogger(ApplyModulesUtil.class);

    private ApplyModulesUtil() {
    }

    public static void applyModules() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

        CopyStrategyLibrary copyStrategyLibrary = CoreRegistry.get(CopyStrategyLibrary.class);
        copyStrategyLibrary.clear();
        for (Class<? extends CopyStrategy> copyStrategy : moduleManager.getEnvironment().getSubtypesOf(CopyStrategy.class)) {
            if (copyStrategy.getAnnotation(RegisterCopyStrategy.class) == null) {
                continue;
            }
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

        ModuleAwareAssetTypeManager assetTypeManager = CoreRegistry.get(ModuleAwareAssetTypeManager.class);
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());


        BlockFamilyFactoryRegistry blockFamilyFactoryRegistry = CoreRegistry.get(BlockFamilyFactoryRegistry.class);
        loadFamilies((DefaultBlockFamilyFactoryRegistry) blockFamilyFactoryRegistry, moduleManager.getEnvironment());
    }

    private static void loadFamilies(DefaultBlockFamilyFactoryRegistry registry, ModuleEnvironment environment) {
        registry.clear();
        for (Class<?> blockFamilyFactory : environment.getTypesAnnotatedWith(RegisterBlockFamilyFactory.class)) {
            if (!BlockFamilyFactory.class.isAssignableFrom(blockFamilyFactory)) {
                logger.error("Cannot load {}, must be a subclass of BlockFamilyFactory", blockFamilyFactory.getSimpleName());
                continue;
            }

            RegisterBlockFamilyFactory registerInfo = blockFamilyFactory.getAnnotation(RegisterBlockFamilyFactory.class);
            String id = registerInfo.value();
            logger.debug("Registering blockFamilyFactory {}", id);
            try {
                BlockFamilyFactory newBlockFamilyFactory = (BlockFamilyFactory) blockFamilyFactory.newInstance();
                registry.setBlockFamilyFactory(id, newBlockFamilyFactory);
                logger.debug("Loaded blockFamilyFactory {}", id);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Failed to load blockFamilyFactory {}", id, e);
            }
        }
    }
}
