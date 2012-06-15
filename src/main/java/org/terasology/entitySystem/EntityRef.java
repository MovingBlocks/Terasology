package org.terasology.entitySystem;

import org.terasology.entitySystem.common.NullEntityRef;

/**
 * A wrapper around an entity id providing access to common functionality
 * @author Immortius <immortius@gmail.com>
 */
public abstract class EntityRef {

    public static final NullEntityRef NULL = NullEntityRef.getInstance();

    /**
     * @return Does this entity exist - that is, is not deleted.
     */
    public abstract boolean exists();

    /**
     * @param component
     * @return If this entity has the given component
     */
    public abstract boolean hasComponent(Class<? extends Component> component);

    /**
     * @param componentClass
     * @param <T>
     * @return The requested component, or null if the entity doesn't have a component of this class
     */
    public abstract <T extends Component> T getComponent(Class<T> componentClass);

    /**
     * Adds a component to this entity. If the entity already has a component of the same class it is replaced.
     * @param component
     */
    public abstract <T extends Component> T addComponent(T component);

    /**
     * @param componentClass
     */
    public abstract void removeComponent(Class<? extends Component> componentClass);

    /**
     * Saves changes made to a component back to the entity manager (otherwise the changes will not persist for
     * future retrievals
     * @param component
     */
    // TODO: Actually need this? Or just save entity?
    public abstract void saveComponent(Component component);
    
    /**
     * Iterates over all the components this entity has
     * @return
     */
    public abstract Iterable<Component> iterateComponents();

    /**
     * Removes all components from this entity, effectively destroying it
     */
    public abstract void destroy();

    /**
     * Transmits an event to this entity
     * @param event
     */
    public abstract void send(Event event);

    /**
     * @return The identifier of this entity. Should be avoided where possible and the EntityRef
     * used instead to allow it to be invalidated if the entity is destroyed.
     */
    public abstract int getId();

}
