package org.terasology.entitySystem.pojo;

import gnu.trove.iterator.TLongObjectIterator;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prototype entity manager. Not intended for final use, but a stand in for experimentation.
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityManager implements EntityManager {

    // TODO: Need to recycle freed ids.
    private long nextEntityId = 1;
    ComponentTable store = new ComponentTable();
    private EventSystem eventSystem;

    public EntityRef create() {
        return new PojoEntityRef(this, nextEntityId++);
    }

    public EntityRef get(long entityId) {
        return new PojoEntityRef(this, entityId);
    }

    public void destroy(long entityId) {
        if (eventSystem != null) {
            eventSystem.send(new PojoEntityRef(this, entityId), RemovedComponentEvent.newInstance());
        }
        store.remove(entityId);
    }

    public <T extends Component> T getComponent(long entityId, Class<T> componentClass) {
        return store.get(entityId, componentClass);
    }

    public <T extends Component> T addComponent(long entityId, T component) {
        Component oldComponent = store.put(entityId, component);
        if (eventSystem != null) {
            if (oldComponent == null) {
                eventSystem.send(new PojoEntityRef(this, entityId), AddComponentEvent.newInstance(), component);
            } else {
                eventSystem.send(new PojoEntityRef(this, entityId), ChangedComponentEvent.newInstance(), component);
            }
        }
        return component;
    }

    public void removeComponent(long entityId, Class<? extends Component> componentClass) {
        Component component = store.get(entityId, componentClass);
        if (component != null) {
            if (eventSystem != null) {
                eventSystem.send(new PojoEntityRef(this, entityId), RemovedComponentEvent.newInstance(), component);
            }
            store.remove(entityId, componentClass);
        }
    }

    public void saveComponent(long entityId, Component component) {
        if (eventSystem != null) {
            eventSystem.send(new PojoEntityRef(this, entityId), ChangedComponentEvent.newInstance(), component);
        }
    }

    public EventSystem getEventSystem() {
        return eventSystem;
    }

    public void setEventSystem(EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    public <T extends Component> Iterable<Map.Entry<EntityRef,T>> iterateComponents(Class<T> componentClass) {
        TLongObjectIterator<T> iterator = store.componentIterator(componentClass);
        if (iterator != null) {
            List<Map.Entry<EntityRef, T>> list = new ArrayList<Map.Entry<EntityRef, T>>();
            while (iterator.hasNext()) {
                iterator.advance();
                list.add(new EntityEntry<T>(new PojoEntityRef(this, iterator.key()), iterator.value()));
            }
            return list;
        }
        return NullIterator.newInstance();
    }

    public Iterable<Component> iterateComponents(long entityId) {
        return store.iterateComponents(entityId);
    }

    public boolean hasComponent(long entityId, Class<? extends Component> componentClass) {
        return store.get(entityId, componentClass) != null;
    }

    private static class EntityEntry<T> implements Map.Entry<EntityRef, T>
    {
        private EntityRef key;
        private T value;
        
        public EntityEntry(EntityRef ref, T value) {
            this.key = ref;
            this.value = value;
        }

        public EntityRef getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }

        public T setValue(T value) {
            throw new UnsupportedOperationException();
        }
    }
}
