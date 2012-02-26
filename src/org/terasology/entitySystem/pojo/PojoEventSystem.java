package org.terasology.entitySystem.pojo;

import com.google.common.collect.*;
import org.terasology.entitySystem.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
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

    public void registerEventHandler(EventHandler handler) {
        Class handlerClass = handler.getClass();
        if (!Modifier.isPublic(handlerClass.getModifiers())) {
            logger.warning(String.format("Cannot register handler %s, must be public", handler.getClass().getName()));
            return;
        }

        // TODO: Refactor
        logger.info("Registering event handler " + handlerClass.getName());
        for (Method method : handlerClass.getMethods())
        {
            ReceiveEvent receiveEventAnnotation = method.getAnnotation(ReceiveEvent.class);
            if (receiveEventAnnotation != null) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    logger.warning(String.format("Cannot register %s of class %s, as it must be public", method.getName(), handlerClass.getName()));
                }
                Class<?>[] types = method.getParameterTypes();
                             
                
                if (types.length == 3 && Event.class.isAssignableFrom(types[0]) && EntityRef.class.isAssignableFrom(types[1]) &&
                        Component.class.isAssignableFrom(types[2]))
                {
                    if (receiveEventAnnotation.components().length == 0) {
                        if (!types[2].equals(Component.class)) {
                            Multimap<Class<? extends Component>, EventHandlerInfo> componentMap = componentSpecificHandlers.get((Class<? extends Event>) types[0]);
                            if (componentMap == null) {
                                componentMap = HashMultimap.create();
                                componentSpecificHandlers.put((Class<? extends Event>) types[0], componentMap);
                            }
                            componentMap.put((Class<? extends Component>)types[2], new EventHandlerInfo(handler, method));
                        }
                    }
                    else {
                        for (Class<? extends Component> c : receiveEventAnnotation.components()) {
                            Multimap<Class<? extends Component>, EventHandlerInfo> componentMap = componentSpecificHandlers.get((Class<? extends Event>) types[0]);
                            if (componentMap == null) {
                                componentMap = HashMultimap.create();
                                componentSpecificHandlers.put((Class<? extends Event>) types[0], componentMap);
                            }
                            componentMap.put(c, new EventHandlerInfo(handler, method));
                        }
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

        for (Class<? extends Component> compClass : handlers.keySet()) {
            Component comp = entity.getComponent(compClass);
            if (comp != null) {
                for (EventHandlerInfo eventHandler : handlers.get(compClass)) {
                    eventHandler.invoke(entity, comp, event);
                }
            }
        }
    }

    public void send(EntityRef entity, Event event, Component component) {
        Multimap<Class<? extends Component>, EventHandlerInfo> handlers = componentSpecificHandlers.get(event.getClass());
        if (handlers != null) {
            for (EventHandlerInfo eventHandler : handlers.get(component.getClass())) {
                eventHandler.invoke(entity, component, event);
            }
        }
    }

    private class EventHandlerInfo
    {
        private EventHandler handler;
        private Method method;

        public EventHandlerInfo(EventHandler handler, Method method)
        {
            this.handler = handler;
            this.method = method;
        }

        public void invoke(EntityRef entity, Component component, Event event)
        {
            try {
                method.invoke(handler, event, entity, component);
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
