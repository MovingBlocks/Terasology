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
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.internal.PrefabDeltaFormat;
import org.terasology.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.copy.RegisterCopyStrategy;
import org.terasology.reflection.reflect.ReflectFactory;
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

    public static void applyModules(Context context) {
        ModuleManager moduleManager = context.get(ModuleManager.class);

        CopyStrategyLibrary copyStrategyLibrary = context.get(CopyStrategyLibrary.class);
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

        ReflectFactory reflectFactory = context.get(ReflectFactory.class);
        TypeSerializationLibrary typeSerializationLibrary = TypeSerializationLibrary.createDefaultLibrary(reflectFactory, copyStrategyLibrary);
        typeSerializationLibrary.add(CollisionGroup.class, new CollisionGroupTypeHandler(context.get(CollisionGroupManager.class)));
        context.put(TypeSerializationLibrary.class, typeSerializationLibrary);

        // Entity System Library
        EntitySystemLibrary library = new EntitySystemLibrary(context, typeSerializationLibrary);
        context.put(EntitySystemLibrary.class, library);
        ComponentLibrary componentLibrary = library.getComponentLibrary();
        context.put(ComponentLibrary.class, componentLibrary);
        context.put(EventLibrary.class, library.getEventLibrary());

        registerComponents(componentLibrary, moduleManager.getEnvironment());

        BlockFamilyFactoryRegistry blockFamilyFactoryRegistry = context.get(BlockFamilyFactoryRegistry.class);
        loadFamilies((DefaultBlockFamilyFactoryRegistry) blockFamilyFactoryRegistry, moduleManager.getEnvironment());

        ModuleAwareAssetTypeManager assetTypeManager = context.get(ModuleAwareAssetTypeManager.class);

        /*
         * The registring of the assset delta formats is done in this method, because it needs to be done before
         * the environment switch. It can't be done before this method gets called because the ComponentLibrary isn't
         * existing then yet.
         *
         * This method is propably something that should be refactored in future.
         */
        PrefabDeltaFormat prefabDeltaFormat = new PrefabDeltaFormat(componentLibrary, typeSerializationLibrary);
        assetTypeManager.registerCoreDeltaFormat(Prefab.class, prefabDeltaFormat);

        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());

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

    private static void registerComponents(ComponentLibrary library, ModuleEnvironment environment) {
        for (Class<? extends Component> componentType : environment.getSubtypesOf(Component.class)) {
            if (componentType.getAnnotation(DoNotAutoRegister.class) == null) {
                String componentName = MetadataUtil.getComponentClassName(componentType);
                library.register(new SimpleUri(environment.getModuleProviding(componentType), componentName), componentType);
            }
        }
    }
}
