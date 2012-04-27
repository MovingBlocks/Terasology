package org.terasology.entitySystem.pojo;

import com.google.common.collect.*;
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

    public PojoEventSystem(EntityManager entitySystem) {
        this.entitySystem = entitySystem;
    }

    public void registerEventHandler(EventHandlerSystem handler) {
        Class handlerClass = handler.getClass();
        if (!Modifier.isPublic(handlerClass.getModifiers())) {
            logger.warning(String.format("Cannot register handler %s, must be public", handler.getClass().getName()));
            return;
        }

        logger.info("Registering event handler " + handlerClass.getName());
        for (Method method : handlerClass.getMethods())
        {
            ReceiveEvent receiveEventAnnotation = method.getAnnotation(ReceiveEvent.class);
            if (receiveEventAnnotation != null) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    method.setAccessible(true);
                }
                Class<?>[] types = method.getParameterTypes();
                             
                
                if (types.length == 2 && Event.class.isAssignableFrom(types[0]) && EntityRef.class.isAssignableFrom(types[1]))
                {
                    logger.info("Found method: " + method.toString());
                    EventHandlerInfo handlerInfo = new EventHandlerInfo(handler, method, receiveEventAnnotation.components(), receiveEventAnnotation.priority());
                    for (Class<? extends Component> c : receiveEventAnnotation.components()) {
                        Multimap<Class<? extends Component>, EventHandlerInfo> componentMap = componentSpecificHandlers.get((Class<? extends Event>) types[0]);
                        if (componentMap == null) {
                            componentMap = HashMultimap.create();
                            componentSpecificHandlers.put((Class<? extends Event>) types[0], componentMap);
                        }
                        componentMap.put(c, handlerInfo);
                    }
                }
                else
                {
                    logger.warning("Invalid event handler method: " + method.getName());
                }
            }
        }
    }

    public void send(EntityRef entity, Event event) {
        Multimap<Class<? extends Component>, EventHandlerInfo> handlers = componentSpecificHandlers.get(event.getClass());
        if (handlers == null) return;

        Set<EventHandlerInfo> selectedHandlersSet = Sets.newHashSet();
        
        for (Class<? extends Component> compClass : handlers.keySet()) {
            if (entity.hasComponent(compClass)) {
                for (EventHandlerInfo eventHandler : handlers.get(compClass)) {
                    if (eventHandler.isValidFor(entity)) {
                        selectedHandlersSet.add(eventHandler);
                    }
                }
            }
        }

        List<EventHandlerInfo> selectedHandlers = Lists.newArrayList(selectedHandlersSet);
        Collections.sort(selectedHandlers, priorityComparator);

        for (EventHandlerInfo handler : selectedHandlers) {
            handler.invoke(entity, event);
            if (event.isCancelled())
                return;
        }
    }

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

    private static class EventHandlerPriorityComparator implements Comparator<EventHandlerInfo> {

        @Override
        public int compare(EventHandlerInfo o1, EventHandlerInfo o2) {
            return o2.getPriority() - o1.getPriority();
        }
    }

    private class EventHandlerInfo
    {
        private EventHandlerSystem handler;
        private Method method;
        private Class<? extends Component>[] components;
        private int priority;

        public EventHandlerInfo(EventHandlerSystem handler, Method method, Class<? extends Component>[] components, int priority)
        {
            this.handler = handler;
            this.method = method;
            this.components = components;
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

        public void invoke(EntityRef entity, Event event)
        {
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
}
