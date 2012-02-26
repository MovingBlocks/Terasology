package org.terasology.entitySystem;

/**
 * A wrapper around an entity id providing access to common functionality
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityRef {
    /**
     * @return The identifier for this entity
     */
    long getId();

    /**
     *
     * @param componentClass
     * @param <T>
     * @return The requested component, or null if the entity doesn't have a component of this class
     */
    <T extends Component> T getComponent(Class<T> componentClass);

    /**
     * Adds a component to this entity. If the entity already has a component of the same class it is replaced.
     * @param component
     */
    <T extends Component> T addComponent(T component);

    /**
     * @param componentClass
     */
    void removeComponent(Class<? extends Component> componentClass);

    /**
     * @param component
     */
    void saveComponent(Component component);
    
    /**
     * Iterates over all the components
     * @return
     */
    Iterable<Component> iterateComponents();

    /**
     * Removes all components from this entity, effectively destroying it
     */
    void destroy();

    /**
     * Transmits an event to this entity
     * @param event
     */
    void send(Event event);
}
