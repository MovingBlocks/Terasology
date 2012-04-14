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

}
