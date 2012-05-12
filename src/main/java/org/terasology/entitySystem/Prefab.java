package org.terasology.entitySystem;

import org.terasology.asset.Asset;

/**
 * An entity prefab describes the recipe for creating an entity.
 * Like an entity it groups a collection of components.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface Prefab {

    /**
     * @return The identifier for this prefab
     */
    public String getName();

    /**
     *
     * @param componentClass
     * @param <T>
     * @return The requested component, or null if the entity doesn't have a component of this class
     */
    public <T extends Component> T getComponent(Class<T> componentClass);

    /**
     * Adds a component to this entity. If the entity already has a component of the same class it is replaced.
     * @param component
     */
    public <T extends Component> T setComponent(T component);

    /**
     * @param componentClass
     */
    public void removeComponent(Class<? extends Component> componentClass);

    /**
     * Iterates over all the components
     * @return
     */
    public Iterable<Component> listComponents();

    /**
     * Iterate only over OWN components, excluding inheritance.
     * Required for proper serializing
     *
     * @return
     */
    public Iterable<Component> listOwnComponents();

    /**
     * Return parents prefabs
     *
     * @return
     */
    public Iterable<Prefab> getParents();

    public void addParent(Prefab parent);

    public void removeParent(Prefab parent);

}
