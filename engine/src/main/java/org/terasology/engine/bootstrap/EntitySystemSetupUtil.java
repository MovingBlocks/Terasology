/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.bootstrap;

import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.event.internal.EventSystemImpl;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.input.events.InputEvent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.module.ModuleEnvironment;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.recording.CharacterStateEventPositionMap;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.EventCatcher;
import org.terasology.recording.EventSystemReplayImpl;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplaySerializer;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.recording.RecordedEventStore;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;

import java.util.ArrayList;
import java.util.List;

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
        TypeSerializationLibrary typeSerializationLibrary = TypeSerializationLibrary.createDefaultLibrary(reflectFactory, copyStrategyLibrary);
        context.put(TypeSerializationLibrary.class, typeSerializationLibrary);
        EntitySystemLibrary library = new EntitySystemLibrary(context, typeSerializationLibrary);
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
     * <li>{@link org.terasology.persistence.typeHandling.TypeSerializationLibrary}</li>
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
        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        NetworkSystem networkSystem = context.get(NetworkSystem.class);

        // Entity Manager
        PojoEntityManager entityManager = new PojoEntityManager();
        context.put(EntityManager.class, entityManager);
        context.put(EngineEntityManager.class, entityManager);

        // Standard serialization library
        TypeSerializationLibrary typeSerializationLibrary = context.get(TypeSerializationLibrary.class);
        typeSerializationLibrary.addTypeHandler(EntityRef.class, new EntityRefTypeHandler(entityManager));
        entityManager.setTypeSerializerLibrary(typeSerializationLibrary);

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
        RecordAndReplaySerializer recordAndReplaySerializer = new RecordAndReplaySerializer(entityManager, recordedEventStore, recordAndReplayUtils, characterStateEventPositionMap, directionAndOriginPosRecorderList, environment);
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

    private static EventSystem createEventSystem(NetworkSystem networkSystem, PojoEntityManager entityManager, EntitySystemLibrary library,
                                                 RecordedEventStore recordedEventStore, RecordAndReplaySerializer recordAndReplaySerializer,
                                                 RecordAndReplayUtils recordAndReplayUtils, RecordAndReplayCurrentStatus recordAndReplayCurrentStatus) {
        EventSystem eventSystem;
        List<Class<?>> selectedClassesToRecord = createSelectedClassesToRecordList();
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.PREPARING_REPLAY) {
            eventSystem = new EventSystemReplayImpl(library.getEventLibrary(), networkSystem, entityManager, recordedEventStore,
                    recordAndReplaySerializer, recordAndReplayUtils, selectedClassesToRecord, recordAndReplayCurrentStatus);
        } else {
            EventCatcher eventCatcher = new EventCatcher(selectedClassesToRecord, recordedEventStore);
            eventSystem = new EventSystemImpl(library.getEventLibrary(), networkSystem, eventCatcher, recordAndReplayCurrentStatus);
        }
        return eventSystem;
    }

    private static void registerComponents(ComponentLibrary library, ModuleEnvironment environment) {
        for (Class<? extends Component> componentType : environment.getSubtypesOf(Component.class)) {
            if (componentType.getAnnotation(DoNotAutoRegister.class) == null) {
                String componentName = MetadataUtil.getComponentClassName(componentType);
                library.register(new SimpleUri(environment.getModuleProviding(componentType), componentName), componentType);
            }
        }
    }

    private static void registerEvents(EventSystem eventSystem, ModuleEnvironment environment) {
        for (Class<? extends Event> type : environment.getSubtypesOf(Event.class)) {
            if (type.getAnnotation(DoNotAutoRegister.class) == null) {
                eventSystem.registerEvent(new SimpleUri(environment.getModuleProviding(type), type.getSimpleName()), type);
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
