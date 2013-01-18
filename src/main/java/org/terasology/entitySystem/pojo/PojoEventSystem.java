/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.pojo;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.EventReceiver;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.Client;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.OwnerEvent;
import org.terasology.network.ServerEvent;
import org.terasology.world.block.BlockComponent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEventSystem implements EventSystem {

    private static final Logger logger = LoggerFactory.getLogger(PojoEventSystem.class);

    private Map<Class<? extends Event>, Multimap<Class<? extends Component>, EventHandlerInfo>> componentSpecificHandlers = Maps.newHashMap();
    private Comparator<EventHandlerInfo> priorityComparator = new EventHandlerPriorityComparator();

    // Event metadata
    private BiMap<String, Class<? extends Event>> eventIdMap = HashBiMap.create();
    private Multimap<Class<? extends Event>, Class<? extends Event>> childEvents = HashMultimap.create();

    private Thread mainThread;
    private BlockingQueue<PendingEvent> pendingEvents = Queues.newLinkedBlockingQueue();

    private EventLibrary eventLibrary;
    private NetworkSystem networkSystem;

    public PojoEventSystem(EventLibrary eventLibrary, NetworkSystem networkSystem) {
        this.mainThread = Thread.currentThread();
        this.eventLibrary = eventLibrary;
        this.networkSystem = networkSystem;
    }

    public void process() {
        for (PendingEvent event = pendingEvents.poll(); event != null; event = pendingEvents.poll()) {
            if (event.getComponent() != null) {
                send(event.getEntity(), event.getEvent(), event.getComponent());
            } else {
                send(event.getEntity(), event.getEvent());
            }
        }
    }

    @Override
    public void registerEvent(String name, Class<? extends Event> eventType) {
        if (name != null && !name.isEmpty()) {
            eventIdMap.put(name, eventType);
        }
        logger.debug("Registering event {}", eventType.getSimpleName());
        for (Class parent : Reflections.getAllSuperTypes(eventType, Predicates.assignableFrom(Event.class))) {
            if (!AbstractEvent.class.equals(parent) && !Event.class.equals(parent)) {
                childEvents.put(parent, eventType);
            }
        }
        if (shouldAddToLibrary(eventType)) {
            eventLibrary.register(eventType, name);
        }
    }

    /**
     * Events are added to the event library if they have a network annotation
     *
     * @param eventType
     * @return Whether the event should be added to the event library
     */
    private boolean shouldAddToLibrary(Class<? extends Event> eventType) {
        return eventType.getAnnotation(ServerEvent.class) != null
                || eventType.getAnnotation(OwnerEvent.class) != null
                || eventType.getAnnotation(BroadcastEvent.class) != null;
    }

    @Override
    public void registerEventHandler(EventHandlerSystem handler) {
        Class handlerClass = handler.getClass();
        // TODO: Support private methods
        if (!Modifier.isPublic(handlerClass.getModifiers())) {
            logger.error("Cannot register handler {}, must be public", handler.getClass().getName());
            return;
        }

        logger.debug("Registering event handler " + handlerClass.getName());
        for (Method method : handlerClass.getMethods()) {
            ReceiveEvent receiveEventAnnotation = method.getAnnotation(ReceiveEvent.class);
            if (receiveEventAnnotation != null) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    method.setAccessible(true);
                }
                Class<?>[] types = method.getParameterTypes();

                if (types.length == 2 && Event.class.isAssignableFrom(types[0]) && EntityRef.class.isAssignableFrom(types[1])) {
                    logger.debug("Found method: " + method.toString());
                    ReflectedEventHandlerInfo handlerInfo = new ReflectedEventHandlerInfo(handler, method, receiveEventAnnotation.priority(), receiveEventAnnotation.components());
                    for (Class<? extends Component> c : receiveEventAnnotation.components()) {
                        addEventHandler((Class<? extends Event>) types[0], handlerInfo, c);
                        for (Class<? extends Event> childType : childEvents.get((Class<? extends Event>) types[0])) {
                            addEventHandler(childType, handlerInfo, c);
                        }
                    }
                } else {
                    logger.error("Invalid event handler method: {}", method.getName());
                }
            }
        }
    }

    private void addEventHandler(Class<? extends Event> type, EventHandlerInfo handlerInfo, Class<? extends Component> c) {
        Multimap<Class<? extends Component>, EventHandlerInfo> componentMap = componentSpecificHandlers.get(type);
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
        EventHandlerInfo info = new ReceiverEventHandlerInfo<T>(eventReceiver, priority, componentTypes);
        for (Class<? extends Component> c : componentTypes) {
            addEventHandler(eventClass, info, c);
            for (Class<? extends Event> childType : childEvents.get(eventClass)) {
                addEventHandler(childType, info, c);
            }
        }
    }

    @Override
    public <T extends Event> void unregisterEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, Class<? extends Component>... componentTypes) {
        Multimap<Class<? extends Component>, EventHandlerInfo> eventHandlerMap = componentSpecificHandlers.get(eventClass);
        if (eventHandlerMap != null) {
            ReceiverEventHandlerInfo testReceiver = new ReceiverEventHandlerInfo<T>(eventReceiver, 0, componentTypes);
            for (Class<? extends Component> c : componentTypes) {
                eventHandlerMap.remove(c, testReceiver);
                for (Class<? extends Event> childType : childEvents.get(eventClass)) {
                    eventHandlerMap.remove(childType, testReceiver);
                }
            }
        }
    }

    @Override
    public void send(EntityRef entity, Event event) {
        if (Thread.currentThread() != mainThread) {
            pendingEvents.offer(new PendingEvent(entity, event));
        } else {
            networkReplicate(entity, event);

            Set<EventHandlerInfo> selectedHandlersSet = selectEventHandlers(event.getClass(), entity);
            List<EventHandlerInfo> selectedHandlers = Lists.newArrayList(selectedHandlersSet);
            Collections.sort(selectedHandlers, priorityComparator);

            for (EventHandlerInfo handler : selectedHandlers) {
                // Check isValid at each stage in case components were removed.
                if (handler.isValidFor(entity)) {
                    handler.invoke(entity, event);
                    if (event.isCancelled())
                        return;
                }
            }
        }
    }

    private void networkReplicate(EntityRef entity, Event event) {
        EventMetadata metadata = eventLibrary.getMetadata(event);
        if (metadata != null && metadata.isNetworkEvent()) {
            logger.info("Replicating event: {}", event);
            switch (metadata.getNetworkEventType()) {
                case BROADCAST:
                    if (networkSystem.getMode() == NetworkMode.SERVER) {
                        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
                        if (netComp != null) {
                            for (Client client : networkSystem.getPlayers()) {
                                client.send(event, netComp.networkId);
                            }
                        }
                    }
                    break;
                case OWNER:
                    if (networkSystem.getMode() == NetworkMode.SERVER) {
                        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
                        if (netComp != null) {
                            Client client = networkSystem.getOwner(entity);
                            if (client != null) {
                                client.send(event, netComp.networkId);
                            }
                        }
                    }
                    break;
                case SERVER:
                    if (networkSystem.getMode() == NetworkMode.CLIENT) {
                        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
                        if (netComp != null) {
                            networkSystem.getServer().send(event, netComp.networkId);
                        }
                    }
                    break;

            }
        }
    }

    @Override
    public void send(EntityRef entity, Event event, Component component) {
        if (Thread.currentThread() != mainThread) {
            pendingEvents.offer(new PendingEvent(entity, event, component));
        } else {
            Multimap<Class<? extends Component>, EventHandlerInfo> handlers = componentSpecificHandlers.get(event.getClass());
            if (handlers != null) {
                for (EventHandlerInfo eventHandler : handlers.get(component.getClass())) {
                    if (eventHandler.isValidFor(entity)) {
                        eventHandler.invoke(entity, event);
                    }
                }
            }
        }
    }

    private Set<EventHandlerInfo> selectEventHandlers(Class<? extends Event> eventType, EntityRef entity) {
        Set<EventHandlerInfo> result = Sets.newHashSet();
        Multimap<Class<? extends Component>, EventHandlerInfo> handlers = componentSpecificHandlers.get(eventType);
        if (handlers == null)
            return result;

        for (Class<? extends Component> compClass : handlers.keySet()) {
            if (entity.hasComponent(compClass)) {
                for (EventHandlerInfo eventHandler : handlers.get(compClass)) {
                    if (eventHandler.isValidFor(entity)) {
                        result.add(eventHandler);
                    }
                }
            }
        }
        return result;
    }

    private static class EventHandlerPriorityComparator implements Comparator<EventHandlerInfo> {

        @Override
        public int compare(EventHandlerInfo o1, EventHandlerInfo o2) {
            return o2.getPriority() - o1.getPriority();
        }
    }

    private interface EventHandlerInfo {
        public boolean isValidFor(EntityRef entity);

        public void invoke(EntityRef entity, Event event);

        public int getPriority();
    }

    private class ReflectedEventHandlerInfo implements EventHandlerInfo {
        private EventHandlerSystem handler;
        private Method method;
        private Class<? extends Component>[] components;
        private int priority;

        public ReflectedEventHandlerInfo(EventHandlerSystem handler, Method method, int priority, Class<? extends Component>... components) {
            this.handler = handler;
            this.method = method;
            this.components = Arrays.copyOf(components, components.length);
            this.priority = priority;
        }

        public boolean isValidFor(EntityRef entity) {
            for (Class<? extends Component> component : components) {
                if (!entity.hasComponent(component)) {
                    return false;
                }
            }
            return true;
        }

        public void invoke(EntityRef entity, Event event) {
            try {
                method.invoke(handler, event, entity);
            } catch (IllegalAccessException ex) {
                logger.error("Failed to invoke event", ex);
            } catch (IllegalArgumentException ex) {
                logger.error("Failed to invoke event", ex);
            } catch (InvocationTargetException ex) {
                logger.error("Failed to invoke event", ex);
            }
        }

        public int getPriority() {
            return priority;
        }
    }

    private class ReceiverEventHandlerInfo<T extends Event> implements EventHandlerInfo {
        private EventReceiver<T> receiver;
        private Class<? extends Component>[] components;
        private int priority;

        public ReceiverEventHandlerInfo(EventReceiver<T> receiver, int priority, Class<? extends Component>... components) {
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
            if (obj instanceof ReceiverEventHandlerInfo) {
                ReceiverEventHandlerInfo other = (ReceiverEventHandlerInfo) obj;
                if (Objects.equal(receiver, other.receiver)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(receiver);
        }
    }
}
