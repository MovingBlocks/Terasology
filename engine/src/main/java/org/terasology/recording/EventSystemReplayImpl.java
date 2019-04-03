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

import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventReceiver;
import org.terasology.entitySystem.event.internal.EventSystem;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.event.ConsumableEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.event.PendingEvent;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.Client;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkEvent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.OwnerEvent;
import org.terasology.network.ServerEvent;
import org.terasology.world.block.BlockComponent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;



/**
 * Event System used during a replay. It works almost the same as EventSystemImpl, with most methods being exactly the
 * same, with the exception of 'send' and 'process'. On the 'process' method, the recorded events are loaded from a file
 * to the RecordedEventStore and then they are processed for a certain amount of time. The 'send' filters which events can
 * be sent by the engine during a replay. This is important to ensure that the recorded events are replayed correctly
 * and that the player does not interfere with the replay.
 */
public class EventSystemReplayImpl implements EventSystem {

    private static final Logger logger = LoggerFactory.getLogger(EventSystemReplayImpl.class);

    private Map<Class<? extends Event>, SetMultimap<Class<? extends Component>, EventHandlerInfo>> componentSpecificHandlers = Maps.newHashMap();
    private SetMultimap<Class<? extends Event>, EventSystemReplayImpl.EventHandlerInfo> generalHandlers = HashMultimap.create();
    private Comparator<EventHandlerInfo> priorityComparator = new EventSystemReplayImpl.EventHandlerPriorityComparator();

    // Event metadata
    private BiMap<SimpleUri, Class<? extends Event>> eventIdMap = HashBiMap.create();
    private SetMultimap<Class<? extends Event>, Class<? extends Event>> childEvents = HashMultimap.create();

    private Thread mainThread;
    private BlockingQueue<PendingEvent> pendingEvents = Queues.newLinkedBlockingQueue();
    private BlockingQueue<RecordedEvent> recordedEvents = Queues.newLinkedBlockingQueue();

    private EventLibrary eventLibrary;
    private NetworkSystem networkSystem;

    //Event replaying
    /** if the recorded events were loaded from the RecordedEventStore. */
    private boolean areRecordedEventsLoaded;
    /** When the events were loaded. Used to reproduce the events at the correct time. */
    private long replayEventsLoadTime;
    /** Necessary to do some entity id mapping from original client and replay client. */
    private EngineEntityManager entityManager;
    /** Where the RecordedEvents are deserialized */
    private RecordedEventStore recordedEventStore;
    /** Class responsible for deserializing recorded data */
    private RecordAndReplaySerializer recordAndReplaySerializer;
    /** Responsible for knowing the game name of the recording */
    private RecordAndReplayUtils recordAndReplayUtils;
    /** List of classes selected to replay */
    private List<Class<?>> selectedClassesToReplay;
    /** The current Status of Record And Replay */
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;
    /** The position of the last recorded event processed */
    private long lastRecordedEventIndex;


    public EventSystemReplayImpl(EventLibrary eventLibrary, NetworkSystem networkSystem, EngineEntityManager entityManager,
                                 RecordedEventStore recordedEventStore, RecordAndReplaySerializer recordAndReplaySerializer,
                                 RecordAndReplayUtils recordAndReplayUtils, List<Class<?>> selectedClassesToReplay,
                                 RecordAndReplayCurrentStatus recordAndReplayCurrentStatus) {
        this.mainThread = Thread.currentThread();
        this.eventLibrary = eventLibrary;
        this.networkSystem = networkSystem;
        this.entityManager = entityManager;
        this.recordedEventStore = recordedEventStore;
        this.recordAndReplaySerializer = recordAndReplaySerializer;
        this.recordAndReplayUtils = recordAndReplayUtils;
        this.selectedClassesToReplay = selectedClassesToReplay;
        this.recordAndReplayCurrentStatus = recordAndReplayCurrentStatus;
    }

    /**
     * Fills recordedEvents with the events in RecordedEventStore.
     */
    private void fillRecordedEvents() {
        Collection<RecordedEvent> events = recordedEventStore.getEvents();
        for (RecordedEvent event : events) {
            this.recordedEvents.offer(event);
        }
    }

    // send method of EventSystemImpl
    private void originalSend(EntityRef entity, Event event) {
        if (Thread.currentThread() != mainThread) {
            pendingEvents.offer(new PendingEvent(entity, event));
        } else {
            networkReplicate(entity, event);

            Set<EventHandlerInfo> selectedHandlersSet = selectEventHandlers(event.getClass(), entity);
            List<EventHandlerInfo> selectedHandlers = Lists.newArrayList(selectedHandlersSet);
            selectedHandlers.sort(priorityComparator);

            if (event instanceof ConsumableEvent) {
                sendConsumableEvent(entity, event, selectedHandlers);
            } else {
                sendStandardEvent(entity, event, selectedHandlers);
            }
        }
    }

    // send method of EventSystemImpl
    private void originalSend(EntityRef entity, Event event, Component component) {

        if (Thread.currentThread() != mainThread) {
            pendingEvents.offer(new PendingEvent(entity, event, component));
        } else {
            SetMultimap<Class<? extends Component>, EventSystemReplayImpl.EventHandlerInfo> handlers = componentSpecificHandlers.get(event.getClass());
            if (handlers != null) {
                List<EventSystemReplayImpl.EventHandlerInfo> eventHandlers = Lists.newArrayList(handlers.get(component.getClass()));
                eventHandlers.sort(priorityComparator);
                for (EventSystemReplayImpl.EventHandlerInfo eventHandler : eventHandlers) {
                    if (eventHandler.isValidFor(entity)) {
                        eventHandler.invoke(entity, event);
                    }
                }
            }
        }
    }

    /**
     * Processes recorded and pending events. If recordedEvents is not loaded, load it from RecordedEventStore.
     */
    @Override
    public void process() {
        processRecordedEvents();
        PendingEvent event = pendingEvents.poll();
        while (event != null) {
            if (event.getComponent() != null) {
                originalSend(event.getEntity(), event.getEvent(), event.getComponent());
            } else {
                originalSend(event.getEntity(), event.getEvent());
            }
            event = pendingEvents.poll();
        }
    }

    /**
     * Processes recorded events for a certain amount of time and only if the timestamp is right.
     */
    private void processRecordedEvents() {
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.REPLAYING && !this.areRecordedEventsLoaded) {
            initialiseReplayData();
        }
        //If replay is ready, process some recorded events if the time is right.
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.REPLAYING) {
            processRecordedEventsBatch(1);
            if (this.recordedEvents.isEmpty()) {
                if (recordAndReplayUtils.getFileCount() <= recordAndReplayUtils.getFileAmount()) { //Get next recorded events file
                    loadNextRecordedEventFile();
                } else {
                    finishReplay();
                }
            }
        }
    }

    /**
     * Empty the RecordedEventStore and sets the RecordAndReplayStatus.
     */
    private void finishReplay() {
        recordedEventStore.popEvents();
        recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.REPLAY_FINISHED); // stops the replay if every recorded event was already replayed
    }

    private void loadNextRecordedEventFile() {
        String recordingPath = PathManager.getInstance().getRecordingPath(recordAndReplayUtils.getGameTitle()).toString();
        recordAndReplaySerializer.deserializeRecordedEvents(recordingPath);
        fillRecordedEvents();
    }

    private void initialiseReplayData() {
        fillRecordedEvents();
        this.areRecordedEventsLoaded = true;
        logger.info("Loaded Recorded Events!");
        replayEventsLoadTime = System.currentTimeMillis();
    }

    /**
     * Try to process recorded events for 'maxDuration' miliseconds. Events are only processed if the time is right.
     * @param maxDuration the amount of time in which this method will try to process recorded events in one go.
     */
    private void processRecordedEventsBatch(long maxDuration) {
        long beginTime = System.currentTimeMillis();
        for (RecordedEvent re = recordedEvents.peek(); re != null; re = recordedEvents.peek()) {
            long passedTime = System.currentTimeMillis() - this.replayEventsLoadTime;
            //Waits until the time of reproduction is right or until 'maxDuration' miliseconds have already passed since this method was called
            while (passedTime < re.getTimestamp()) {
                passedTime = System.currentTimeMillis() - this.replayEventsLoadTime;
                if ((System.currentTimeMillis() - beginTime) >= maxDuration) {
                    return;
                }
            }
            recordedEvents.poll();
            EntityRef entity = getEntityRef(re);
            // Sends recorded event to be processed
            if (re.getComponent() != null) {
                originalSend(entity, re.getEvent(), re.getComponent());
            } else {
                originalSend(entity, re.getEvent());
            }
            this.lastRecordedEventIndex = re.getIndex();
            // Check if time is up.
            if ((System.currentTimeMillis() - beginTime) >= maxDuration) {
                return;
            }
        }
    }

    /**
     * Since only the EntityRef's id is saved in the RecordedEvent, it is necessary to get the real EntityRef when
     * processing a RecordedEvent.
     * @param recordedEvent The recorded event containing the ID of the entity to be gotten.
     * @return the EntityRef with the id recorded in the RecordedEvent.
     */
    private EntityRef getEntityRef(RecordedEvent recordedEvent) {
        return this.entityManager.getEntity(recordedEvent.getEntityId());
    }

    @Override
    public void registerEvent(SimpleUri uri, Class<? extends Event> eventType) {
        eventIdMap.put(uri, eventType);
        logger.debug("Registering event {}", eventType.getSimpleName());
        for (Class parent : ReflectionUtils.getAllSuperTypes(eventType, Predicates.assignableFrom(Event.class))) {
            if (!AbstractConsumableEvent.class.equals(parent) && !Event.class.equals(parent)) {
                childEvents.put(parent, eventType);
            }
        }
        if (shouldAddToLibrary(eventType)) {
            eventLibrary.register(uri, eventType);
        }
    }

    /**
     * Events are added to the event library if they have a network annotation
     *
     * @param eventType the type of the event to be checked.
     * @return Whether the event should be added to the event library
     */
    private boolean shouldAddToLibrary(Class<? extends Event> eventType) {
        return eventType.getAnnotation(ServerEvent.class) != null
                || eventType.getAnnotation(OwnerEvent.class) != null
                || eventType.getAnnotation(BroadcastEvent.class) != null;
    }

    @Override
    public void registerEventHandler(ComponentSystem handler) {
        Class handlerClass = handler.getClass();
        if (!Modifier.isPublic(handlerClass.getModifiers())) {
            logger.error("Cannot register handler {}, must be public", handler.getClass().getName());
            return;
        }

        logger.debug("Registering event handler " + handlerClass.getName());
        for (Method method : handlerClass.getMethods()) {
            ReceiveEvent receiveEventAnnotation = method.getAnnotation(ReceiveEvent.class);
            if (receiveEventAnnotation != null) {
                if (!receiveEventAnnotation.netFilter().isValidFor(networkSystem.getMode(), false)) {
                    continue;
                }
                Set<Class<? extends Component>> requiredComponents = Sets.newLinkedHashSet();
                method.setAccessible(true);
                Class<?>[] types = method.getParameterTypes();

                logger.debug("Found method: " + method.toString());
                if (!Event.class.isAssignableFrom(types[0]) || !EntityRef.class.isAssignableFrom(types[1])) {
                    logger.error("Invalid event handler method: {}", method.getName());
                    return;
                }

                requiredComponents.addAll(Arrays.asList(receiveEventAnnotation.components()));
                List<Class<? extends Component>> componentParams = Lists.newArrayList();
                for (int i = 2; i < types.length; ++i) {
                    if (!Component.class.isAssignableFrom(types[i])) {
                        logger.error("Invalid event handler method: {} - {} is not a component class", method.getName(), types[i]);
                        return;
                    }
                    requiredComponents.add((Class<? extends Component>) types[i]);
                    componentParams.add((Class<? extends Component>) types[i]);
                }

                EventSystemReplayImpl.ByteCodeEventHandlerInfo handlerInfo = new EventSystemReplayImpl.ByteCodeEventHandlerInfo(handler, method, receiveEventAnnotation.priority(),
                        receiveEventAnnotation.activity(), requiredComponents, componentParams);
                addEventHandler((Class<? extends Event>) types[0], handlerInfo, requiredComponents);
            }
        }
    }

    @Override
    public void unregisterEventHandler(ComponentSystem handler) {
        for (SetMultimap<Class<? extends Component>, EventSystemReplayImpl.EventHandlerInfo> eventHandlers : componentSpecificHandlers.values()) {
            Iterator<EventHandlerInfo> eventHandlerIterator = eventHandlers.values().iterator();
            while (eventHandlerIterator.hasNext()) {
                EventSystemReplayImpl.EventHandlerInfo eventHandler = eventHandlerIterator.next();
                if (eventHandler.getHandler().equals(handler)) {
                    eventHandlerIterator.remove();
                }
            }
        }

        Iterator<EventSystemReplayImpl.EventHandlerInfo> eventHandlerIterator = generalHandlers.values().iterator();
        while (eventHandlerIterator.hasNext()) {
            EventSystemReplayImpl.EventHandlerInfo eventHandler = eventHandlerIterator.next();
            if (eventHandler.getHandler().equals(handler)) {
                eventHandlerIterator.remove();
            }
        }
    }

    private void addEventHandler(Class<? extends Event> type, EventSystemReplayImpl.EventHandlerInfo handler, Collection<Class<? extends Component>> components) {
        if (components.isEmpty()) {
            generalHandlers.put(type, handler);
            for (Class<? extends Event> childType : childEvents.get(type)) {
                generalHandlers.put(childType, handler);
            }
        } else {
            for (Class<? extends Component> c : components) {
                addToComponentSpecificHandlers(type, handler, c);
                for (Class<? extends Event> childType : childEvents.get(type)) {
                    addToComponentSpecificHandlers(childType, handler, c);
                }
            }
        }
    }

    private void addToComponentSpecificHandlers(Class<? extends Event> type, EventSystemReplayImpl.EventHandlerInfo handlerInfo, Class<? extends Component> c) {
        SetMultimap<Class<? extends Component>, EventSystemReplayImpl.EventHandlerInfo> componentMap = componentSpecificHandlers.get(type);
        if (componentMap == null) {
            componentMap = HashMultimap.create();
            componentSpecificHandlers.put(type, componentMap);
        }
        componentMap.put(c, handlerInfo);
    }

    @Override
    public <T extends Event> void registerEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, Class<? extends Component>... componentTypes) {
        registerEventReceiver(eventReceiver, eventClass, EventPriority.PRIORITY_NORMAL, componentTypes);
    }

    @Override
    public <T extends Event> void registerEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, int priority, Class<? extends Component>... componentTypes) {
        EventSystemReplayImpl.EventHandlerInfo info = new EventSystemReplayImpl.ReceiverEventHandlerInfo<>(eventReceiver, priority, componentTypes);
        addEventHandler(eventClass, info, Arrays.asList(componentTypes));
    }

    @Override
    public <T extends Event> void unregisterEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, Class<? extends Component>... componentTypes) {
        SetMultimap<Class<? extends Component>, EventSystemReplayImpl.EventHandlerInfo> eventHandlerMap = componentSpecificHandlers.get(eventClass);
        if (eventHandlerMap != null) {
            EventSystemReplayImpl.ReceiverEventHandlerInfo testReceiver = new EventSystemReplayImpl.ReceiverEventHandlerInfo<>(eventReceiver, 0, componentTypes);
            for (Class<? extends Component> c : componentTypes) {
                eventHandlerMap.remove(c, testReceiver);
                for (Class<? extends Event> childType : childEvents.get(eventClass)) {
                    eventHandlerMap.remove(childType, testReceiver);
                }
            }
        }

        if (0 == componentTypes.length) {
            Iterator<EventSystemReplayImpl.EventHandlerInfo> eventHandlerIterator = generalHandlers.values().iterator();
            while (eventHandlerIterator.hasNext()) {
                EventSystemReplayImpl.EventHandlerInfo eventHandler = eventHandlerIterator.next();
                if (eventHandler.getHandler().equals(eventReceiver)) {
                    eventHandlerIterator.remove();
                }
            }
        }
    }

    /**
     * Calls the 'process' method if the replay is activated and the event is of a type selected to be replayed.
     * This way, events of the types that are recorded and replayed are ignored during a replay. This is what makes
     * the player have no control over the character during a replay.
     * @param entity the entity which the event was sent against.
     * @param event the event being sent.
     */
    @Override
    public void send(EntityRef entity, Event event) {
        if (recordAndReplayCurrentStatus.getStatus() != RecordAndReplayStatus.REPLAYING || !isSelectedToReplayEvent(event)) {
            originalSend(entity, event);
        }
    }

    /**
     * Calls the 'process' method if the replay is activated and the event is of a type selected to be replayed.
     * This way, events of the types that are recorded and replayed are ignored during a replay. This is what makes
     * the player have no control over the character during a replay.
     * @param entity the entity which the event was sent against.
     * @param event the event being sent.
     * @param component the component sent along with the event.
     */
    @Override
    public void send(EntityRef entity, Event event, Component component) {
        if (recordAndReplayCurrentStatus.getStatus() != RecordAndReplayStatus.REPLAYING || !isSelectedToReplayEvent(event)) {
            originalSend(entity, event, component);
        }
    }

    /**
     * Check if the event is selected to be replayed. If they are, they are only processed through replay and sending them
     * normally won't have any effect. Example: Since MouseWheelEvent is selected to replay, during the replay process
     * if the player moves the mouse wheel, the MouseWheelEvents generated won't be processed, but the ones on the recorded
     * events list will.
     * @param event event to be checked
     * @return if the event is selected to replay
     */
    private boolean isSelectedToReplayEvent(Event event) {
        boolean selectedToReplay = false;
        for (Class<?> supportedEventClass : this.selectedClassesToReplay) {
            if (supportedEventClass.isInstance(event)) {
                selectedToReplay = true;
                break;
            }
        }
        return selectedToReplay;

    }

    private void sendStandardEvent(EntityRef entity, Event event, List<EventSystemReplayImpl.EventHandlerInfo> selectedHandlers) {
        for (EventSystemReplayImpl.EventHandlerInfo handler : selectedHandlers) {
            // Check isValid at each stage in case components were removed.
            if (handler.isValidFor(entity)) {
                handler.invoke(entity, event);
            }
        }
    }

    private void sendConsumableEvent(EntityRef entity, Event event, List<EventSystemReplayImpl.EventHandlerInfo> selectedHandlers) {
        ConsumableEvent consumableEvent = (ConsumableEvent) event;
        for (EventSystemReplayImpl.EventHandlerInfo handler : selectedHandlers) {
            // Check isValid at each stage in case components were removed.
            if (handler.isValidFor(entity)) {
                handler.invoke(entity, event);
                if (consumableEvent.isConsumed()) {
                    return;
                }
            }
        }
    }

    private void networkReplicate(EntityRef entity, Event event) {
        EventMetadata metadata = eventLibrary.getMetadata(event);
        if (metadata != null && metadata.isNetworkEvent()) {
            logger.debug("Replicating event: {}", event);
            switch (metadata.getNetworkEventType()) {
                case BROADCAST:
                    broadcastEvent(entity, event, metadata);
                    break;
                case OWNER:
                    sendEventToOwner(entity, event);
                    break;
                case SERVER:
                    sendEventToServer(entity, event);
                    break;
                default:
                    break;
            }
        }
    }

    private void sendEventToServer(EntityRef entity, Event event) {
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
            if (netComp != null) {
                networkSystem.getServer().send(event, entity);
            }
        }
    }

    private void sendEventToOwner(EntityRef entity, Event event) {
        if (networkSystem.getMode().isServer()) {
            NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
            if (netComp != null) {
                Client client = networkSystem.getOwner(entity);
                if (client != null) {
                    client.send(event, entity);
                }
            }
        }
    }

    private void broadcastEvent(EntityRef entity, Event event, EventMetadata metadata) {
        if (networkSystem.getMode().isServer()) {
            NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
            BlockComponent blockComp = entity.getComponent(BlockComponent.class);
            if (netComp != null || blockComp != null) {
                Client instigatorClient = null;
                if (metadata.isSkipInstigator() && event instanceof NetworkEvent) {
                    instigatorClient = networkSystem.getOwner(((NetworkEvent) event).getInstigator());
                }
                for (Client client : networkSystem.getPlayers()) {
                    if (!client.equals(instigatorClient)) {
                        client.send(event, entity);
                    }
                }
            }
        }
    }


    private Set<EventSystemReplayImpl.EventHandlerInfo> selectEventHandlers(Class<? extends Event> eventType, EntityRef entity) {
        Set<EventSystemReplayImpl.EventHandlerInfo> result = Sets.newHashSet();
        result.addAll(generalHandlers.get(eventType));
        SetMultimap<Class<? extends Component>, EventSystemReplayImpl.EventHandlerInfo> handlers = componentSpecificHandlers.get(eventType);
        if (handlers == null) {
            return result;
        }

        for (Class<? extends Component> compClass : handlers.keySet()) {
            if (entity.hasComponent(compClass)) {
                for (EventSystemReplayImpl.EventHandlerInfo eventHandler : handlers.get(compClass)) {
                    if (eventHandler.isValidFor(entity)) {
                        result.add(eventHandler);
                    }
                }
            }
        }
        return result;
    }

    public long getLastRecordedEventIndex() {
        return lastRecordedEventIndex;
    }

    private static class EventHandlerPriorityComparator implements Comparator<EventSystemReplayImpl.EventHandlerInfo> {

        @Override
        public int compare(EventSystemReplayImpl.EventHandlerInfo o1, EventSystemReplayImpl.EventHandlerInfo o2) {
            return o2.getPriority() - o1.getPriority();
        }
    }

    private interface EventHandlerInfo {
        boolean isValidFor(EntityRef entity);

        void invoke(EntityRef entity, Event event);

        int getPriority();

        Object getHandler();
    }

    private static class ByteCodeEventHandlerInfo implements EventSystemReplayImpl.EventHandlerInfo {
        private ComponentSystem handler;
        private String activity;
        private MethodAccess methodAccess;
        private int methodIndex;
        private ImmutableList<Class<? extends Component>> filterComponents;
        private ImmutableList<Class<? extends Component>> componentParams;
        private int priority;

        ByteCodeEventHandlerInfo(ComponentSystem handler,
                                 Method method,
                                 int priority,
                                 String activity,
                                 Collection<Class<? extends Component>> filterComponents,
                                 Collection<Class<? extends Component>> componentParams) {


            this.handler = handler;
            this.activity = activity;
            this.methodAccess = MethodAccess.get(handler.getClass());
            methodIndex = methodAccess.getIndex(method.getName(), method.getParameterTypes());
            this.filterComponents = ImmutableList.copyOf(filterComponents);
            this.componentParams = ImmutableList.copyOf(componentParams);
            this.priority = priority;
        }

        @Override
        public boolean isValidFor(EntityRef entity) {
            for (Class<? extends Component> component : filterComponents) {
                if (!entity.hasComponent(component)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void invoke(EntityRef entity, Event event) {
            try {
                Object[] params = new Object[2 + componentParams.size()];
                params[0] = event;
                params[1] = entity;
                for (int i = 0; i < componentParams.size(); ++i) {
                    params[i + 2] = entity.getComponent(componentParams.get(i));
                }
                if (!activity.isEmpty()) {
                    PerformanceMonitor.startActivity(activity);
                }
                try {
                    methodAccess.invoke(handler, methodIndex, params);
                } finally {
                    if (!activity.isEmpty()) {
                        PerformanceMonitor.endActivity();
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to invoke event", ex);
            }
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public ComponentSystem getHandler() {
            return handler;
        }
    }

    private static class ReceiverEventHandlerInfo<T extends Event> implements EventSystemReplayImpl.EventHandlerInfo {
        private EventReceiver<T> receiver;
        private Class<? extends Component>[] components;
        private int priority;

        ReceiverEventHandlerInfo(EventReceiver<T> receiver, int priority, Class<? extends Component>... components) {
            this.receiver = receiver;
            this.priority = priority;
            this.components = Arrays.copyOf(components, components.length);
        }

        @Override
        public boolean isValidFor(EntityRef entity) {
            for (Class<? extends Component> component : components) {
                if (!entity.hasComponent(component)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void invoke(EntityRef entity, Event event) {
            receiver.onEvent((T) event, entity);
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof EventSystemReplayImpl.ReceiverEventHandlerInfo) {
                EventSystemReplayImpl.ReceiverEventHandlerInfo other = (EventSystemReplayImpl.ReceiverEventHandlerInfo) obj;
                return Objects.equal(receiver, other.receiver);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(receiver);
        }

        @Override
        public Object getHandler() {
            return receiver;
        }
    }


}
