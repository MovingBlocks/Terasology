package org.terasology.entitySystem;

/**
 * @author Immortius
 */
public interface MutableComponentContainer extends ComponentContainer {
    /**
     * Adds a component. If this already has a component of the same class it is replaced.
     *
     * @param component
     */
    <T extends Component> T addComponent(T component);

    /**
     * @param componentClass
     */
    void removeComponent(Class<? extends Component> componentClass);

    /**
     * Saves changes made to a component
     *
     * @param component
     */
    void saveComponent(Component component);
}
