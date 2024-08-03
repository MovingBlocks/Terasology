// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.recording;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.input.binds.interaction.AttackButton;
import org.terasology.engine.input.events.InputEvent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventSystemReplayImplTest {

    private EntityRef entity;
    private EventSystem eventSystem;
    private TestEventHandler handler;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    @BeforeEach
    public void setup() {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);

        Reflections reflections = new Reflections(getClass().getClassLoader());
        TypeHandlerLibrary serializationLibrary = new TypeHandlerLibraryImpl(reflections);

        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, serializationLibrary);
        PojoEntityManager entityManager = new PojoEntityManager();
        entityManager.setComponentLibrary(entitySystemLibrary.getComponentLibrary());
        entityManager.setPrefabManager(new PojoPrefabManager(context));
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        recordAndReplayCurrentStatus = new RecordAndReplayCurrentStatus();
        RecordedEventStore eventStore = new RecordedEventStore();
        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
        ModuleManager moduleManager = mock(ModuleManager.class);
        when(moduleManager.getEnvironment()).thenReturn(mock(ModuleEnvironment.class));
        RecordAndReplaySerializer recordAndReplaySerializer = new RecordAndReplaySerializer(entityManager, eventStore,
                recordAndReplayUtils, characterStateEventPositionMap, directionAndOriginPosRecorderList, moduleManager, mock(TypeRegistry.class));
        recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.REPLAYING);
        entity = entityManager.create();
        Long id = entity.getId();
        eventStore.add(new RecordedEvent(id, new AttackButton(), 1, 1));
        eventStore.add(new RecordedEvent(id, new AttackButton(), 2, 2));
        eventStore.add(new RecordedEvent(id, new AttackButton(), 3, 3));

        List<Class<?>> selectedClassesToReplay = new ArrayList<>();
        selectedClassesToReplay.add(InputEvent.class);

        eventSystem = new EventSystemReplayImpl(entitySystemLibrary.getEventLibrary(), networkSystem, entityManager,
                eventStore, recordAndReplaySerializer, recordAndReplayUtils, selectedClassesToReplay, recordAndReplayCurrentStatus);

        entityManager.setEventSystem(eventSystem);

        handler = new TestEventHandler();
        eventSystem.registerEventHandler(handler);
    }

    @Test
    public void testReplayStatus() {
        assertEquals(RecordAndReplayStatus.REPLAYING, recordAndReplayCurrentStatus.getStatus());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 100) {
            eventSystem.process();
        }
        assertEquals(RecordAndReplayStatus.REPLAY_FINISHED, recordAndReplayCurrentStatus.getStatus());
    }

    @Test
    public void testProcessingRecordedEvent() {
        assertEquals(0, handler.receivedAttackButtonList.size());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 100) {
            eventSystem.process();
        }
        assertEquals(3, handler.receivedAttackButtonList.size());
    }

    @Test
    public void testBlockingEventDuringReplay() {
        assertEquals(0, handler.receivedAttackButtonList.size());
        eventSystem.send(entity, new AttackButton());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 100) {
            eventSystem.process();
        }
        assertEquals(3, handler.receivedAttackButtonList.size());
    }

    @Test
    public void testSendingEventAfterReplay() {
        assertEquals(0, handler.receivedAttackButtonList.size());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 100) {
            eventSystem.process();
        }
        eventSystem.send(entity, new AttackButton());
        assertEquals(4, handler.receivedAttackButtonList.size());
    }

    @Test
    public void testSendingAllowedEventDuringReplay() {
        eventSystem.send(entity, new TestEvent());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 100) {
            eventSystem.process();
        }
        assertEquals(3, handler.receivedAttackButtonList.size());
        assertEquals(1, handler.receivedTestEventList.size());
    }

    @AfterEach
    public void cleanStates() {
        recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.NOT_ACTIVATED);
    }

    public static class TestEventHandler extends BaseComponentSystem {

        List<Received> receivedAttackButtonList = Lists.newArrayList();
        List<Received> receivedTestEventList = Lists.newArrayList();

        @ReceiveEvent
        public void handleAttackButtonEvent(AttackButton event, EntityRef entity) {
            receivedAttackButtonList.add(new Received(event, entity));
        }

        @ReceiveEvent
        public void handleTestEvent(TestEvent event, EntityRef entity) {
            receivedTestEventList.add(new Received(event, entity));
        }
    }

    public static class Received {
        Event event;
        EntityRef entity;

        Received(Event event, EntityRef entity) {
            this.event = event;
            this.entity = entity;
        }
    }

    public static class TestEvent extends AbstractConsumableEvent {

    }
}
