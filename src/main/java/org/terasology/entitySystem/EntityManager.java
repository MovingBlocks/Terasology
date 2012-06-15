package org.terasology.entitySystem;

import org.terasology.entitySystem.metadata.ComponentLibrary;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityManager {

    void clear();

    // Entity Management

    /**
     * @return A references to a new, unused entity
     */
    EntityRef create();

    /**
     * @param prefabName The name of the prefab to create.
     * @return A new entity, based on the the prefab of the given name. If the prefab doesn't exist, just a new entity.
     */
    EntityRef create(String prefabName);

    /**
     * @param prefab
     * @return A new entity, based on the given prefab
     */
    EntityRef create(Prefab prefab);

    /**
     * @param componentClass
     * @return The number of entities with this component class
     */
    int getComponentCount(Class<? extends Component> componentClass);

    Iterable<EntityRef> iteratorEntities();

    Iterable<EntityRef> iteratorEntities(Class<? extends Component>... componentClasses);

    <T extends Component> Iterable<Map.Entry<EntityRef, T>> iterateComponents(Class<T> componentClass);


    /**
     * @return The event system being used by the entity manager
     */
    EventSystem getEventSystem();

    void setEventSystem(EventSystem system);

    PrefabManager getPrefabManager();

    ComponentLibrary getComponentLibrary();

}
