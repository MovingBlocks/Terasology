package org.terasology.entitySystem.pojo;

import com.google.common.base.Predicates;
import com.google.common.collect.*;
import org.reflections.Reflections;
import org.terasology.entitySystem.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEventSystem implements EventSystem {

    private Logger logger = Logger.getLogger(getClass().getName());

    private EntityManager entitySystem;
    private Map<Class<? extends Event>, Multimap<Class<? extends Component>, EventHandlerInfo>> componentSpecificHandlers = Maps.newHashMap();
    private Comparator<EventHandlerInfo> priorityComparator = new EventHandlerPriorityComparator();

    // Event metadata
    private BiMap<String, Class<? extends Event>> eventIdMap = HashBiMap.create();
    private Multimap<Class<? extends Event>, Class<? extends Event>> childEvents = HashMultimap.create();


    public PojoEventSystem(EntityManager entitySystem) {
        this.entitySystem = entitySystem;
    }

    @Override
    public void registerEvent(String name, Class<? extends Event> eventType) {
        if (name != null && !name.isEmpty()) {
            eventIdMap.put(name, eventType);
        }
        logger.info("Registering event " + eventType.getSimpleName());
        for (Class parent : Reflections.getAllSuperTypes(eventType, Predicates.assignableFrom(Event.class))) {
            if (!AbstractEvent.class.equals(parent) && !Event.class.equals(parent)) {
                childEvents.put(parent, eventType);
            }
        }
    }

    @Override
    public void registerEventHandler(EventHandlerSystem handler) {
        Class handlerClass = handler.getClass();
        // TODO: Support private methods
        if (!Modifier.isPublic(handlerClass.getModifiers())) {
            logger.warning(String.format("Cannot register handler %s, must be public", handler.getClass().getName()));
            return;
        }

        logger.info("Registering event handler " + handlerClass.getName());
        for (Method method : handlerClass.getMethods()) {
            ReceiveEvent receiveEventAnnotation = method.getAnnotation(ReceiveEvent.class);
            if (receiveEventAnnotation != null) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    method.setAccessible(true);
                }
                Class<?>[] types = method.getParameterTypes();

                if (types.length == 2 && Event.class.isAssignableFrom(types[0]) && EntityRef.class.isAssignableFrom(types[1])) {
                    logger.info("Found method: " + method.toString());
                    ReflectedEventHandlerInfo handlerInfo = new ReflectedEventHandlerInfo(handler, method, receiveEventAnnotation.priority(), receiveEventAnnotation.components());
                    for (Class<? extends Component> c : receiveEventAnnotation.components()) {
                        addEventHandler((Class<? extends Event>) types[0], handlerInfo, c);
                        for (Class<? extends Event> childType : childEvents.get((Class<? extends Event>) types[0])) {
                            addEventHandler(childType, handlerInfo, c);
                        }
                    }
                } else {
                    logger.warning("Invalid event handler method: " + method.getName());
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
    public void send(EntityRef entity, Event event) {
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

    @Override
    public void send(EntityRef entity, Event event, Component component) {
        Multimap<Class<? extends Component>, EventHandlerInfo> handlers = componentSpecificHandlers.get(event.getClass());
        if (handlers != null) {
            for (EventHandlerInfo eventHandler : handlers.get(component.getClass())) {
                if (eventHandler.isValidFor(entity)) {
                    eventHandler.invoke(entity, event);
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
                logger.log(Level.SEVERE, "Failed to invoke event", ex);
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, "Failed to invoke event", ex);
            } catch (InvocationTargetException ex) {
                logger.log(Level.SEVERE, "Failed to invoke event", ex);
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
    }
}
