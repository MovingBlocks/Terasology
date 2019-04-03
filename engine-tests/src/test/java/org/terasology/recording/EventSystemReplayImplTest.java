/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.recording;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.input.binds.interaction.AttackButton;
import org.terasology.input.events.InputEvent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventSystemReplayImplTest {

    private EntityRef entity;
    private EventSystem eventSystem;
    private TestEventHandler handler;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;



    @Before
    public void setup() {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
        TypeSerializationLibrary serializationLibrary = new TypeSerializationLibrary(reflectFactory, copyStrategies);
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
        RecordAndReplaySerializer recordAndReplaySerializer = new RecordAndReplaySerializer(entityManager, eventStore, recordAndReplayUtils, characterStateEventPositionMap, directionAndOriginPosRecorderList, null);
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
        while ((System.currentTimeMillis() - startTime) < 30) {
            eventSystem.process();
        }
        assertEquals(RecordAndReplayStatus.REPLAY_FINISHED, recordAndReplayCurrentStatus.getStatus());
    }

    @Test
    public void testProcessingRecordedEvent() {
        assertEquals(0, handler.receivedAttackButtonList.size());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 10) {
            eventSystem.process();
        }
        assertEquals(3, handler.receivedAttackButtonList.size());
    }

    @Test
    public void testBlockingEventDuringReplay() {
        assertEquals(0, handler.receivedAttackButtonList.size());
        eventSystem.send(entity, new AttackButton());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 10) {
            eventSystem.process();
        }
        assertEquals(3, handler.receivedAttackButtonList.size());
    }

    @Test
    public void testSendingEventAfterReplay() {
        assertEquals(0, handler.receivedAttackButtonList.size());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 10) {
            eventSystem.process();
        }
        eventSystem.send(entity, new AttackButton());
        assertEquals(4, handler.receivedAttackButtonList.size());
    }

    @Test
    public void testSendingAllowedEventDuringReplay() {
        eventSystem.send(entity, new TestEvent());
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 10) {
            eventSystem.process();
        }
        assertEquals(3, handler.receivedAttackButtonList.size());
        assertEquals(1, handler.receivedTestEventList.size());
    }



    @After
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

    private static class TestEvent extends AbstractConsumableEvent {

    }




}
