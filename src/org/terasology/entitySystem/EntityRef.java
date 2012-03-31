package org.terasology.entitySystem;

/**
 * A wrapper around an entity id providing access to common functionality
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityRef {

    /**
     * Whether this is a null entity. Null entities do not have components
     * and cannot have components.
     *
     * @return Whether this entity exists.
     */
    boolean isNull();

    /**
     *
     * @param component
     * @return If this entity has the given component
     */
    boolean hasComponent(Class<? extends Component> component);

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
     * Saves changes made to a component back to the entity manager (otherwise the changes will not persist for
     * future retrievals
     * @param component
     */
    // TODO: Actually need this? Or just save entity?
    void saveComponent(Component component);
    
    /**
     * Iterates over all the components this entity has
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
