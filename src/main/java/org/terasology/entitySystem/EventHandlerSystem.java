package org.terasology.entitySystem;

/**
 * Interface for classes that can be registered as event handlers
 * 
 * Methods of EventHandlers that receive events need to be marked up with the 
 * {@link org.terasology.entitySystem.ReceiveEvent} annotation
 * @author Immortius <immortius@gmail.com>
 */
public interface EventHandlerSystem extends ComponentSystem {
}
