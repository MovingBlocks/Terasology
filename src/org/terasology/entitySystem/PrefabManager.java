package org.terasology.entitySystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface PrefabManager {

    /**
     * @param name
     * @return A new PrefabRef
     * @throws IllegalArgumentException when name is already in use
     */
    PrefabRef create(String name);

    PrefabRef get(String name);
    
    boolean exists(String name);

    /**
     * @param oldName
     * @param name
     * @return A new PrefabRef with the new name
     * @throws IllegalArgumentException if either the oldName isn't in use or the new name is already in use
     */
    PrefabRef rename(String oldName, String name);

    void destroy(String name);

    <T extends Component> T getComponent(String name, Class<T> componentClass);
    void addComponent(String name, Component component);
    <T extends Component> void removeComponent(String name, Class<T> componentClass);
    void saveComponent(String name, Component component);
}
