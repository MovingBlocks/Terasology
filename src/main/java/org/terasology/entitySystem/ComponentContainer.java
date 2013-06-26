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
     * Iterates over all the components this entity has
     *
     * @return
     */
    public abstract Iterable<Component> iterateComponents();

}
