package org.terasology.entitySystem;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityManager {

    EventSystem getEventSystem();
    
    // Entity Management

    /**
     * @return A references to a new, unused entity
     */
    EntityRef createEntity();

    //TODO: Not sure this should be exposed

    /**
     * @param entityId
     * @return A reference to an existing entity, or null if it doesn't exist
     */
    EntityRef getEntity(long entityId);

    /**
     * Strips an entity of all its components and destroys it
     * @param entityId
     */
    void destroyEntity(long entityId);
    
    // Component Management
    <T extends Component> T getComponent(long entityId, Class<T> componentClass);
    <T extends Component> T addComponent(long entityId, T component);
    void removeComponent(long entityId, Class<? extends Component> componentClass);
    void saveComponent(long entityId, Component component);
    
    <T extends Component> Iterable<Map.Entry<EntityRef,T>> iterateComponents(Class<T> componentClass);
    Iterable<Component> iterateComponents(long entityId);
    

    // Methods for starting/ending tasks?
    //void startTask();
    //void commitTask() throws ConcurrentModificationException;
    
    // Entity Prefab Management (these probably should go in a separate class)

    /* EntityRef instantiate(long prefabId)
    
    EntityPrefabRef createEntityPrefab();
    EntityPrefabRef createEntityPrefab(String name);
    EntityPrefabRef getEntityPrefab(long prefabId);
    EntityPrefabRef getEntityPrefab(String name);
    EntityPrefabRef changeEntityPrefabName(long prefabId, String name);
    EntityPrefabRef removeEntityPrefabName(long prefabId);
    void destroyEntityPrefab(long prefabId);
    void destroyEntityPrefab(String name);

    <T extends Component> T getPrefabComponent(long prefabId, Class<T> componentClass);
    void addPrefabComponent(long prefabId, Component component);
    <T extends Component> void removePrefabComponent(long prefabId, Class<T> componentClass);
    void savePrefabComponent(Component component);*/

}
