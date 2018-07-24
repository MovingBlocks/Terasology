/*
 * Copyright 2018 MovingBlocks
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

import java.lang.reflect.Type;
import java.util.Optional;

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
import org.terasology.entitySystem.prefab.internal.PrefabFormat;
import org.terasology.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.RegisterTypeHandler;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.copy.RegisterCopyStrategy;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.InjectionHelper;
import org.terasology.util.reflection.GenericsUtil;
import org.terasology.utilities.ReflectionUtil;

/**
 * Handles an environment switch by updating the asset manager, component library, and other context objects.
 */
public final class EnvironmentSwitchHandler {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentSwitchHandler.class);

    private PrefabFormat registeredPrefabFormat;
    private PrefabDeltaFormat registeredPrefabDeltaFormat;

    public EnvironmentSwitchHandler() {
    }

    @SuppressWarnings("unchecked")
    public void handleSwitchToGameEnvironment(Context context) {
        ModuleManager moduleManager = context.get(ModuleManager.class);

        CopyStrategyLibrary copyStrategyLibrary = context.get(CopyStrategyLibrary.class);
        copyStrategyLibrary.clear();
        for (Class<? extends CopyStrategy> copyStrategy : moduleManager.getEnvironment().getSubtypesOf(CopyStrategy.class)) {
            if (copyStrategy.getAnnotation(RegisterCopyStrategy.class) == null) {
                continue;
            }
            Type targetType = ReflectionUtil.getTypeParameterForSuper(copyStrategy, CopyStrategy.class, 0);
            if (targetType instanceof Class) {
                registerCopyStrategy(copyStrategyLibrary, (Class<?>) targetType, copyStrategy);
            } else {
                logger.error("Cannot register CopyStrategy '{}' - unable to determine target type", copyStrategy);
            }
        }

        ReflectFactory reflectFactory = context.get(ReflectFactory.class);
        TypeSerializationLibrary typeSerializationLibrary = TypeSerializationLibrary.createDefaultLibrary(reflectFactory, copyStrategyLibrary);
        typeSerializationLibrary.addTypeHandler(CollisionGroup.class, new CollisionGroupTypeHandler(context.get(CollisionGroupManager.class)));
        context.put(TypeSerializationLibrary.class, typeSerializationLibrary);

        // Entity System Library
        EntitySystemLibrary library = new EntitySystemLibrary(context, typeSerializationLibrary);
        context.put(EntitySystemLibrary.class, library);
        ComponentLibrary componentLibrary = library.getComponentLibrary();
        context.put(ComponentLibrary.class, componentLibrary);
        context.put(EventLibrary.class, library.getEventLibrary());
        context.put(ClassMetaLibrary.class, new ClassMetaLibraryImpl(context));

        registerComponents(componentLibrary, moduleManager.getEnvironment());
        registerTypeHandlers(context, typeSerializationLibrary, moduleManager.getEnvironment());

        ModuleAwareAssetTypeManager assetTypeManager = context.get(ModuleAwareAssetTypeManager.class);

        /*
         * The registering of the prefab formats is done in this method, because it needs to be done before
         * the environment of the asset manager gets changed.
         *
         * It can't be done before this method gets called because the ComponentLibrary isn't
         * existing then yet.
         */
        unregisterPrefabFormats(assetTypeManager);
        registeredPrefabFormat = new PrefabFormat(componentLibrary, typeSerializationLibrary);
        assetTypeManager.registerCoreFormat(Prefab.class, registeredPrefabFormat);
        registeredPrefabDeltaFormat = new PrefabDeltaFormat(componentLibrary, typeSerializationLibrary);
        assetTypeManager.registerCoreDeltaFormat(Prefab.class, registeredPrefabDeltaFormat);

        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());

    }

    private <T, U extends CopyStrategy<T>> void registerCopyStrategy(CopyStrategyLibrary copyStrategyLibrary, Class<T> type, Class<U> strategy) {
        try {
            copyStrategyLibrary.register(type, strategy.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Cannot register CopyStrategy '{}' - failed to instantiate", strategy, e);
        }
    }

    /**
     * Switches the environment of the asset manager to the specified one. It does not register the prefab formats
     * as they require a proper ComponentLibrary.
     *
     * The existence of this method call is questionable. It has only be introduced to make sure that
     * the asset type manager has never prefab formats that reference an old ComponentLibrary.
     *
     */
    private void cheapAssetManagerUpdate(Context context, ModuleEnvironment environment) {
        ModuleAwareAssetTypeManager moduleAwareAssetTypeManager = context.get(ModuleAwareAssetTypeManager.class);
        unregisterPrefabFormats(moduleAwareAssetTypeManager);
        moduleAwareAssetTypeManager.switchEnvironment(environment);
    }


    public void handleSwitchToPreviewEnvironment(Context context, ModuleEnvironment environment) {
        cheapAssetManagerUpdate(context, environment);
        ComponentLibrary library = new ComponentLibrary(context);
        context.put(ComponentLibrary.class, library);

        registerComponents(library, environment);
    }

    public void handleSwitchBackFromPreviewEnvironment(Context context) {
        // The newly created ComponentLibrary instance cannot be invalidated in context
        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        cheapAssetManagerUpdate(context, environment);
    }


    public void handleSwitchToEmptyEnvironment(Context context) {
        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        cheapAssetManagerUpdate(context, environment);
    }

    private void unregisterPrefabFormats(ModuleAwareAssetTypeManager assetTypeManager) {
        if (registeredPrefabFormat != null) {
            assetTypeManager.removeCoreFormat(Prefab.class, registeredPrefabFormat);
            registeredPrefabFormat = null;
        }
        if (registeredPrefabDeltaFormat != null) {
            assetTypeManager.removeCoreDeltaFormat(Prefab.class, registeredPrefabDeltaFormat);
            registeredPrefabDeltaFormat = null;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerTypeHandlers(Context context, TypeSerializationLibrary library, ModuleEnvironment environment) {
        for (Class<? extends TypeHandler> handler : environment.getSubtypesOf(TypeHandler.class)) {
            RegisterTypeHandler register = handler.getAnnotation(RegisterTypeHandler.class);
            if (register != null) {
                Optional<Type> opt = GenericsUtil.getTypeParameterBindingForInheritedClass(handler, TypeHandler.class, 0);
                if (opt.isPresent()) {
                    TypeHandler instance = InjectionHelper.createWithConstructorInjection(handler, context);
                    InjectionHelper.inject(instance, context);
                    library.addTypeHandler((Class) opt.get(), instance);
                }
            }
        }
    }
}
