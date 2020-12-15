// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.flexible.AutoConfigManager;
import org.terasology.context.Context;
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
import org.terasology.persistence.typeHandling.RegisterTypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.copy.RegisterCopyStrategy;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.InjectionHelper;
import org.terasology.util.reflection.GenericsUtil;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Optional;

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
        ModuleEnvironment environment = moduleManager.getEnvironment();

        TypeRegistry typeRegistry = context.get(TypeRegistry.class);
        typeRegistry.reload(environment);

        CopyStrategyLibrary copyStrategyLibrary = context.get(CopyStrategyLibrary.class);
        copyStrategyLibrary.clear();
        for (Class<? extends CopyStrategy> copyStrategy : environment.getSubtypesOf(CopyStrategy.class)) {
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

        //TODO: find a permanent fix over just creating a new typehandler
        // https://github.com/Terasology/JoshariasSurvival/issues/31
        // TypeHandlerLibrary typeHandlerLibrary = context.get(TypeHandlerLibrary.class);
        // typeHandlerLibrary.addTypeHandler(CollisionGroup.class, new CollisionGroupTypeHandler(context.get(CollisionGroupManager.class)));

        TypeHandlerLibrary typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);
        typeHandlerLibrary.addTypeHandler(CollisionGroup.class, new CollisionGroupTypeHandler(context.get(CollisionGroupManager.class)));
        context.put(TypeHandlerLibrary.class, typeHandlerLibrary);

        // Entity System Library
        EntitySystemLibrary library = new EntitySystemLibrary(context, typeHandlerLibrary);
        context.put(EntitySystemLibrary.class, library);
        ComponentLibrary componentLibrary = library.getComponentLibrary();
        context.put(ComponentLibrary.class, componentLibrary);
        context.put(EventLibrary.class, library.getEventLibrary());
        context.put(ClassMetaLibrary.class, new ClassMetaLibraryImpl(context));

        registerComponents(componentLibrary, environment);
        registerTypeHandlers(context, typeHandlerLibrary, environment);

        // Load configs for the new environment
        AutoConfigManager autoConfigManager = context.get(AutoConfigManager.class);
        autoConfigManager.loadConfigsIn(context);

        ModuleAwareAssetTypeManager assetTypeManager = context.get(ModuleAwareAssetTypeManager.class);

        /*
         * The registering of the prefab formats is done in this method, because it needs to be done before
         * the environment of the asset manager gets changed.
         *
         * It can't be done before this method gets called because the ComponentLibrary isn't
         * existing then yet.
         */
        unregisterPrefabFormats(assetTypeManager);
        registeredPrefabFormat = new PrefabFormat(componentLibrary, typeHandlerLibrary);
        assetTypeManager.registerCoreFormat(Prefab.class, registeredPrefabFormat);
        registeredPrefabDeltaFormat = new PrefabDeltaFormat(componentLibrary, typeHandlerLibrary);
        assetTypeManager.registerCoreDeltaFormat(Prefab.class, registeredPrefabDeltaFormat);

        assetTypeManager.switchEnvironment(environment);

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
        ComponentLibrary library = new ComponentLibrary(environment, context.get(ReflectFactory.class), context.get(CopyStrategyLibrary.class));
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
                library.register(new ResourceUrn(environment.getModuleProviding(componentType).toString(), componentName), componentType);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerTypeHandlers(Context context, TypeHandlerLibrary library, ModuleEnvironment environment) {
        for (Class<? extends TypeHandler> handler : environment.getSubtypesOf(TypeHandler.class)) {
            RegisterTypeHandler register = handler.getAnnotation(RegisterTypeHandler.class);
            if (register != null) {
                Optional<Type> opt = GenericsUtil.getTypeParameterBindingForInheritedClass(handler, TypeHandler.class, 0);
                if (opt.isPresent()) {
                    TypeHandler instance = InjectionHelper.createWithConstructorInjection(handler, context);
                    InjectionHelper.inject(instance, context);
                    library.addTypeHandler(TypeInfo.of(opt.get()), instance);
                }
            }
        }

        for (Class<? extends TypeHandlerFactory> clazz : environment.getSubtypesOf(TypeHandlerFactory.class)) {
            if (!clazz.isAnnotationPresent(RegisterTypeHandlerFactory.class)) {
                continue;
            }

            TypeHandlerFactory instance = InjectionHelper.createWithConstructorInjection(clazz, context);
            InjectionHelper.inject(instance, context);
            library.addTypeHandlerFactory(instance);
        }
    }
}
