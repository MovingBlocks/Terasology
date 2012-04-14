package org.terasology.entitySystem;

/**
 * Event system propagates events to registered handlers
 * @author Immortius <immortius@gmail.com>
 */
public interface EventSystem {

    /**
     * Registers an object as an event handler - all methods with the {@link ReceiveEvent} annotation will be registered
     * @param handler
     */
    void registerEventHandler(EventHandlerSystem handler);

    /**
     * Sends an event to all handlers for an entity's components
     * @param entity
     * @param event
     */
    void send(EntityRef entity, Event event);

    /**
     * Sends an event to a handlers for a specific component of an entity
     * @param entity
     * @param event
     * @param component
     */
    void send(EntityRef entity, Event event, Component component);
}
