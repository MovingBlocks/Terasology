// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.bootstrap;

import org.terasology.context.Lifetime;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.event.internal.EventSystemImpl;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.entitySystem.metadata.MetadataUtil;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.engine.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.engine.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.engine.input.events.InputEvent;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.network.NetworkEventSystemDecorator;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.engine.recording.EventCatcher;
import org.terasology.engine.recording.EventSystemReplayImpl;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.recording.RecordedEventStore;
import org.terasology.engine.recording.RecordingClasses;
import org.terasology.engine.recording.RecordingEventSystemDecorator;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import javax.inject.Inject;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Provides static methods that can be used to put entity system related objects into a {@link Context} instance.
 */
public final class EntitySystemSetupUtil {

    private EntitySystemSetupUtil() {
        // static utility class, no instance needed
    }

    public static void addReflectionBasedLibraries(ServiceRegistry serviceRegistry) {
        ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
        serviceRegistry.with(ReflectFactory.class).lifetime(Lifetime.Singleton).use(() -> reflectFactory);
        serviceRegistry.with(CopyStrategyLibrary.class).lifetime(Lifetime.Singleton).use(CopyStrategyLibrary.class);
        serviceRegistry.with(TypeHandlerLibrary.class).lifetime(Lifetime.Singleton).use(TypeHandlerLibraryImpl.class);
        serviceRegistry.with(ComponentLibrary.class).lifetime(Lifetime.Singleton).use(ComponentLibrary.class);
        serviceRegistry.with(EventLibrary.class).lifetime(Lifetime.Singleton).use(EventLibrary.class);
        serviceRegistry.with(EntitySystemLibrary.class).lifetime(Lifetime.Singleton).use(EntitySystemLibrary.class);
    }

    public static void addEntityManagementRelatedClasses(ServiceRegistry serviceRegistry) {
        // Prefab Manager
        serviceRegistry.with(PrefabManager.class).lifetime(Lifetime.Singleton).use(PojoPrefabManager.class);

        // Entity Manager
        serviceRegistry.with(PojoEntityManager.class).lifetime(Lifetime.Singleton);

        //Record and Replay
        RecordedEventStore recordedEventStore = new RecordedEventStore();
        serviceRegistry.with(RecordedEventStore.class).lifetime(Lifetime.Singleton).use(() -> recordedEventStore);
        serviceRegistry.with(RecordAndReplaySerializer.class).lifetime(Lifetime.Singleton).use(RecordAndReplaySerializer.class);

        // Event System
        // TODO: Provide RecordAndReplayCurrentStatus
        registerEventSystem(new RecordAndReplayCurrentStatus(), serviceRegistry);

        // TODO: Review - NodeClassLibrary related to the UI for behaviours. Should not be here and probably not even in the CoreRegistry
        OneOfProviderFactory oneOfProviderFactory = new OneOfProviderFactory();
        serviceRegistry.with(OneOfProviderFactory.class).lifetime(Lifetime.Singleton).use(() -> oneOfProviderFactory);
    }

    public static void configureEntityManagementRelatedClasses(TypeHandlerLibrary typeHandlerLibrary,
                                                               EntitySystemLibrary entitySystemLibrary,
                                                               ModuleEnvironment environment, EngineEntityManager entityManager) {
        // Standard serialization library
        typeHandlerLibrary.addTypeHandler(EntityRef.class, new EntityRefTypeHandler(entityManager));
        registerComponents(entitySystemLibrary.getComponentLibrary(), environment);
        registerEvents(entityManager.getEventSystem(), environment);
    }

    /**
     * Objects for the following classes must be available in the context:
     * <ul>
     * <li>{@link ModuleEnvironment}</li>
     * <li>{@link NetworkSystem}</li>
     * <li>{@link ReflectFactory}</li>
     * <li>{@link CopyStrategyLibrary}</li>
     * <li>{@link TypeHandlerLibrary}</li>
     * </ul>
     * <p>
     * The method will make objects for the following classes available in the context:
     * <ul>
     * <li>{@link EngineEntityManager}</li>
     * <li>{@link ComponentLibrary}</li>
     * <li>{@link EventLibrary}</li>
     * <li>{@link PrefabManager}</li>
     * <li>{@link EventSystem}</li>
     * </ul>
     * @deprecated Retained only for compatibility - use {@link #addEntityManagementRelatedClasses(ServiceRegistry)} instead.
     */
    @Deprecated
    public static void addEntityManagementRelatedClasses(Context context, ServiceRegistry serviceRegistry) {
        addEntityManagementRelatedClasses(serviceRegistry);
    }

    private static void registerEventSystem(RecordAndReplayCurrentStatus recordAndReplayCurrentStatus, ServiceRegistry serviceRegistry) {
        RecordingClasses recordingClasses = new RecordingClasses(createSelectedClassesToRecordList());
        serviceRegistry.with(RecordingClasses.class).lifetime(Lifetime.Singleton).use(() -> recordingClasses);
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.NOT_ACTIVATED) {
            serviceRegistry.with(EventSystem.class).lifetime(Lifetime.Singleton).use(NetworkEventSystemWrapped.class);
        } else if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.PREPARING_REPLAY) {
            serviceRegistry.with(EventSystem.class).lifetime(Lifetime.Singleton).use(EventSystemReplayImpl.class);
        } else {
            serviceRegistry.with(EventSystem.class).lifetime(Lifetime.Singleton).use(RecordingEventSystemWrapped.class);
        }
    }

    public static void registerComponents(ComponentLibrary library, ModuleEnvironment environment) {
        for (Class<? extends Component> componentType : environment.getSubtypesOf(Component.class)) {
            if (componentType.getAnnotation(DoNotAutoRegister.class) == null
                    && !componentType.isInterface()
                    && !Modifier.isAbstract(componentType.getModifiers())) {
                String componentName = MetadataUtil.getComponentClassName(componentType);
                Name componentModuleName = verifyNotNull(environment.getModuleProviding(componentType),
                        "Could not find module for %s %s", componentName, componentType);
                library.register(new ResourceUrn(componentModuleName.toString(), componentName), componentType);
            }
        }
    }

    public static void registerEvents(EventSystem eventSystem, ModuleEnvironment environment) {
        for (Class<? extends Event> type : environment.getSubtypesOf(Event.class)) {
            if (type.getAnnotation(DoNotAutoRegister.class) == null) {
                Name module = verifyNotNull(environment.getModuleProviding(type),
                        "Environment has no module for %s", type.getSimpleName());
                eventSystem.registerEvent(new ResourceUrn(module.toString(), type.getSimpleName()), type);
            }
        }
    }

    private static List<Class<?>> createSelectedClassesToRecordList() {
        List<Class<?>> selectedClassesToRecord = new ArrayList<>();
        selectedClassesToRecord.add(InputEvent.class);
        selectedClassesToRecord.add(PlaySoundEvent.class);
        selectedClassesToRecord.add(CameraTargetChangedEvent.class);
        selectedClassesToRecord.add(CharacterMoveInputEvent.class);
        //selectedClassesToRecord.add(AttackEvent.class);
        //selectedClassesToRecord.add(GetMaxSpeedEvent.class);
        return selectedClassesToRecord;
    }

    public static final class NetworkEventSystemWrapped extends NetworkEventSystemDecorator {
        @Inject
        public NetworkEventSystemWrapped(NetworkSystem networkSystem, EntitySystemLibrary entitySystemLibrary) {
            super(new EventSystemImpl(networkSystem.getMode().isAuthority()),
                            networkSystem, entitySystemLibrary.getEventLibrary());
        }
    }

    public static final class RecordingEventSystemWrapped extends RecordingEventSystemDecorator {
        @Inject
        public RecordingEventSystemWrapped(RecordingClasses recordingClasses, NetworkSystem networkSystem,
                                           EntitySystemLibrary entitySystemLibrary,
                                           RecordedEventStore recordedEventStore,
                                           RecordAndReplayCurrentStatus recordAndReplayCurrentStatus) {
            super(new NetworkEventSystemDecorator(new EventSystemImpl(networkSystem.getMode().isAuthority()),
                    networkSystem, entitySystemLibrary.getEventLibrary()), new EventCatcher(recordingClasses, recordedEventStore),
                    recordAndReplayCurrentStatus);
        }
    }
}
