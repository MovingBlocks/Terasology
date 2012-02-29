package org.terasology.entitySystem;

import org.terasology.components.LocationComponent;
import org.terasology.components.MeshComponent;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityManager {

    EventSystem getEventSystem();
    void setEventSystem(EventSystem systen);
    
    // Entity Management

    /**
     * @return A references to a new, unused entity
     */
    EntityRef create();

    /**
     * @param entityId
     * @return A reference to an existing entity, or null if it doesn't exist
     */
    //TODO: Not sure this should be exposed
    EntityRef get(long entityId);

    /**
     * Strips an entity of all its components and destroys it
     * @param entityId
     */
    void destroy(long entityId);
    
    // Component Management
    boolean hasComponent(long entityId, Class<? extends Component> componentClass);
    <T extends Component> T getComponent(long entityId, Class<T> componentClass);
    <T extends Component> T addComponent(long entityId, T component);
    void removeComponent(long entityId, Class<? extends Component> componentClass);
    void saveComponent(long entityId, Component component);
    
    <T extends Component> Iterable<Map.Entry<EntityRef,T>> iterateComponents(Class<T> componentClass);
    Iterable<Component> iterateComponents(long entityId);
    Iterable<EntityRef> iteratorEntities(Class<? extends Component> ...  componentClasses);
}
