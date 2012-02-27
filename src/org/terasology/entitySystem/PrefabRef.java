package org.terasology.entitySystem;

/**
 * An entity prefab describes the recipe for creating an entity.
 * Like an entity it groups a collection of components.
 * @author Immortius <immortius@gmail.com>
 */
public interface PrefabRef {
    /**
     * @return The identifier for this prefab
     */
    String getName();

    /**
     *
     * @param newName
     * @throws IllegalArgumentException If the new name is already in use
     */
    void rename(String newName);

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
     * Removes this prefab from the prefab manager
     */
    void destroy();

}
