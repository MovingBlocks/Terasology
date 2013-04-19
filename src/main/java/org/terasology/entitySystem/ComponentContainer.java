package org.terasology.entitySystem;

/**
 * @author Immortius
 */
public interface ComponentContainer {

    /**
     * @param component
     * @return If this has a component of the given type
     */
    public abstract boolean hasComponent(Class<? extends Component> component);

    /**
     * @param componentClass
     * @param <T>
     * @return The requested component, or null if the this doesn't have a component of this type
     */
    public abstract <T extends Component> T getComponent(Class<T> componentClass);

    /**
     * Adds a component. If this already has a component of the same class it is replaced.
     *
     * @param component
     */
    public abstract <T extends Component> T addComponent(T component);

    /**
     * @param componentClass
     */
    public abstract void removeComponent(Class<? extends Component> componentClass);

    /**
     * Saves changes made to a component
     *
     * @param component
     */
    public abstract void saveComponent(Component component);

    /**
     * Iterates over all the components this entity has
     *
     * @return
     */
    public abstract Iterable<Component> iterateComponents();

}
