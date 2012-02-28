package org.terasology.entitySystem;

/**
 *
 * @todo Write javadoc
 * @author Immortius <immortius@gmail.com>
 */
public interface PrefabManager {

    public Prefab createPrefab(String name);

    public Prefab getPrefab(String name);
    
    public boolean exists(String name);

    public Prefab registerPrefab(Prefab prefab);

    public Iterable<Prefab> listPrefabs();

    public void removePrefab(String name);

    public <T extends Component> T getComponent(String name, Class<T> componentClass);

    public <T extends Component> T setComponent(String name, T component);


    public <T extends Component> void removeComponent(String name, Class<T> componentClass);

}
