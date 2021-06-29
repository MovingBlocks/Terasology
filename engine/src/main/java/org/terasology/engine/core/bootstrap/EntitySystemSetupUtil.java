// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.bootstrap;

import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.event.Event;
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
import org.terasology.engine.recording.CharacterStateEventPositionMap;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.EventCatcher;
import org.terasology.engine.recording.EventSystemReplayImpl;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.recording.RecordedEventStore;
import org.terasology.engine.recording.RecordingEventSystemDecorator;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeRegistry;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

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

    public static void addReflectionBasedLibraries(Context context) {
        ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
        context.put(ReflectFactory.class, reflectFactory);
        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
        context.put(CopyStrategyLibrary.class, copyStrategyLibrary);

        ModuleManager moduleManager = context.get(ModuleManager.class);
        TypeRegistry typeRegistry = context.get(TypeRegistry.class);
        TypeHandlerLibrary typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);
        context.put(TypeHandlerLibrary.class, typeHandlerLibrary);

        EntitySystemLibrary library = new EntitySystemLibrary(context, typeHandlerLibrary);
        context.put(EntitySystemLibrary.class, library);
        context.put(ComponentLibrary.class, library.getComponentLibrary());
        context.put(EventLibrary.class, library.getEventLibrary());
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
     */
    public static void addEntityManagementRelatedClasses(Context context) {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        ModuleEnvironment environment = moduleManager.getEnvironment();
        NetworkSystem networkSystem = context.get(NetworkSystem.class);

        // Entity Manager
        PojoEntityManager entityManager = new PojoEntityManager();
        context.put(EntityManager.class, entityManager);
        context.put(EngineEntityManager.class, entityManager);

        // Standard serialization library
        TypeHandlerLibrary typeHandlerLibrary = context.get(TypeHandlerLibrary.class);
        typeHandlerLibrary.addTypeHandler(EntityRef.class, new EntityRefTypeHandler(entityManager));
        entityManager.setTypeSerializerLibrary(typeHandlerLibrary);

        // Prefab Manager
        PrefabManager prefabManager = new PojoPrefabManager(context);
        entityManager.setPrefabManager(prefabManager);
        context.put(PrefabManager.class, prefabManager);

        EntitySystemLibrary library = context.get(EntitySystemLibrary.class);
        entityManager.setComponentLibrary(library.getComponentLibrary());

        //Record and Replay
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);
        RecordAndReplayUtils recordAndReplayUtils = context.get(RecordAndReplayUtils.class);
        CharacterStateEventPositionMap characterStateEventPositionMap = context.get(CharacterStateEventPositionMap.class);
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = context.get(DirectionAndOriginPosRecorderList.class);
        RecordedEventStore recordedEventStore = new RecordedEventStore();
        RecordAndReplaySerializer recordAndReplaySerializer =
                new RecordAndReplaySerializer(entityManager, recordedEventStore, recordAndReplayUtils, characterStateEventPositionMap,
                        directionAndOriginPosRecorderList, moduleManager, context.get(TypeRegistry.class));
        context.put(RecordAndReplaySerializer.class, recordAndReplaySerializer);


        // Event System
        EventSystem eventSystem = createEventSystem(networkSystem, entityManager, library, recordedEventStore,
                recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
        entityManager.setEventSystem(eventSystem);
        context.put(EventSystem.class, eventSystem);

        // TODO: Review - NodeClassLibrary related to the UI for behaviours. Should not be here and probably not even in the CoreRegistry
        context.put(OneOfProviderFactory.class, new OneOfProviderFactory());
        registerComponents(library.getComponentLibrary(), environment);
        registerEvents(entityManager.getEventSystem(), environment);
    }

    private static EventSystem createEventSystem(NetworkSystem networkSystem,
                                                 PojoEntityManager entityManager,
                                                 EntitySystemLibrary library,
                                                 RecordedEventStore recordedEventStore,
                                                 RecordAndReplaySerializer recordAndReplaySerializer,
                                                 RecordAndReplayUtils recordAndReplayUtils,
                                                 RecordAndReplayCurrentStatus recordAndReplayCurrentStatus) {
        EventSystem eventSystem;
        List<Class<?>> selectedClassesToRecord = createSelectedClassesToRecordList();
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.PREPARING_REPLAY) {
            eventSystem = new EventSystemReplayImpl(library.getEventLibrary(), networkSystem, entityManager, recordedEventStore,
                    recordAndReplaySerializer, recordAndReplayUtils, selectedClassesToRecord, recordAndReplayCurrentStatus);
        } else {
            EventCatcher eventCatcher = new EventCatcher(selectedClassesToRecord, recordedEventStore);
            eventSystem = new EventSystemImpl(networkSystem.getMode().isAuthority());
            eventSystem = new NetworkEventSystemDecorator(eventSystem, networkSystem, library.getEventLibrary());
            eventSystem = new RecordingEventSystemDecorator(eventSystem, eventCatcher, recordAndReplayCurrentStatus);
        }
        return eventSystem;
    }

    static void registerComponents(ComponentLibrary library, ModuleEnvironment environment) {
        for (Class<? extends Component> componentType : environment.getSubtypesOf(Component.class)) {
            if (componentType.getAnnotation(DoNotAutoRegister.class) == null && !componentType.isInterface() && !Modifier.isAbstract(componentType.getModifiers())) {
                String componentName = MetadataUtil.getComponentClassName(componentType);
                Name componentModuleName = verifyNotNull(environment.getModuleProviding(componentType),
                        "Could not find module for %s %s", componentName, componentType);
                library.register(new ResourceUrn(componentModuleName.toString(), componentName), componentType);
            }
        }
    }

    private static void registerEvents(EventSystem eventSystem, ModuleEnvironment environment) {
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

}
