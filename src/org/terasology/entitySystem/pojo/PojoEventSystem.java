package org.terasology.entitySystem.pojo;

import com.google.common.collect.*;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEventSystem implements EventSystem {

    private Logger logger = Logger.getLogger(getClass().getName()); 
    
    private EntityManager entitySystem;
    private Map<Class<? extends Event>, Multimap<Class<? extends Component>, EventHandlerInfo>> componentSpecificHandlers = Maps.newHashMap();

    public PojoEventSystem(EntityManager entitySystem) {
        this.entitySystem = entitySystem;
    }

    public void registerEventHandler(EventHandlerSystem handler) {
        Class handlerClass = handler.getClass();
        if (!Modifier.isPublic(handlerClass.getModifiers())) {
            logger.warning(String.format("Cannot register handler %s, must be public", handler.getClass().getName()));
            return;
        }

        // TODO: Refactor
        // TODO: Work with non-public methods
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
                    EventHandlerInfo handlerInfo = new EventHandlerInfo(handler, method, receiveEventAnnotation.components());
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

        Set<EventHandlerInfo> processedHandlers = Sets.newHashSet();
        
        for (Class<? extends Component> compClass : handlers.keySet()) {
            if (entity.hasComponent(compClass)) {
                for (EventHandlerInfo eventHandler : handlers.get(compClass)) {
                    if (eventHandler.isValidFor(entity)) {
                        if (processedHandlers.add(eventHandler)) {
                            eventHandler.invoke(entity, event);
                        }
                    }
                }
            }
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

    private class EventHandlerInfo
    {
        private EventHandlerSystem handler;
        private Method method;
        private Class<? extends Component>[] components;

        public EventHandlerInfo(EventHandlerSystem handler, Method method, Class<? extends Component>[] components)
        {
            this.handler = handler;
            this.method = method;
            this.components = components;
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
    }
}
