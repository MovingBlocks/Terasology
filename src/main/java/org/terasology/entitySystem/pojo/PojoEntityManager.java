package org.terasology.entitySystem.pojo;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Prototype entity manager. Not intended for final use, but a stand in for experimentation.
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityManager implements EntityManager, PersistableEntityManager {
    public static final int NULL_ID = 0;

    private static Logger logger = Logger.getLogger(PojoEntityManager.class.getName());

    private int nextEntityId = 1;
    private TIntList freedIds = new TIntArrayList();
    private Map<EntityRef, PojoEntityRef> entityCache = new WeakHashMap<EntityRef, PojoEntityRef>();

    private ComponentTable store = new ComponentTable();
    private EventSystem eventSystem;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;

    public PojoEntityManager(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        componentLibrary.registerTypeHandler(EntityRef.class, new EntityRefTypeHandler(this));
    }

    @Override
    public void clear() {
        store.clear();
        nextEntityId = 1;
        freedIds.clear();
        entityCache.clear();
    }

    @Override
    public EntityRef create() {
        if (!freedIds.isEmpty()) {
            createEntityRef(freedIds.removeAt(freedIds.size() - 1));
        }
        if (nextEntityId == NULL_ID) nextEntityId++;
        return createEntityRef(nextEntityId++);
    }

    @Override
    public EntityRef create(String prefabName) {
        if (prefabName != null && !prefabName.isEmpty()) {
            Prefab prefab = prefabManager.getPrefab(prefabName);
            if (prefab == null) {
                logger.log(Level.WARNING, "Unable to instantiate unknown prefab: \"" + prefabName + "\"");
            }
            return create(prefab);
        }
        return create();
    }

    @Override
    public EntityRef create(Prefab prefab) {
        EntityRef result = create();
        if (prefab != null) {
            for (Component component : prefab.listComponents()) {
                result.addComponent(componentLibrary.copy(component));
            }
            result.addComponent(new EntityInfoComponent(prefab.getName()));
        }
        return result;
    }

	@Override
    public int getComponentCount(Class<? extends Component> componentClass) {
        return store.getComponentCount(componentClass);
    }

	@Override
    public EventSystem getEventSystem() {
        return eventSystem;
    }

	@Override
    public void setEventSystem(EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

	@Override
    public PrefabManager getPrefabManager() {
        return prefabManager;
    }

	@Override
    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

	@Override
    public <T extends Component> Iterable<Map.Entry<EntityRef,T>> iterateComponents(Class<T> componentClass) {
        TIntObjectIterator<T> iterator = store.componentIterator(componentClass);
        if (iterator != null) {
            List<Map.Entry<EntityRef, T>> list = new ArrayList<Map.Entry<EntityRef, T>>();
            while (iterator.hasNext()) {
                iterator.advance();
                list.add(new EntityEntry<T>(createEntityRef(iterator.key()), iterator.value()));
            }
            return list;
        }
        return NullIterator.newInstance();
    }

    public Iterable<EntityRef> iteratorEntities() {
        return new Iterable<EntityRef>() {
            public Iterator<EntityRef> iterator() {
                return new EntityIterator(store.entityIdIterator());
            }
        };
    }

    public Iterable<EntityRef> iteratorEntities(Class<? extends Component>... componentClasses) {
        if (componentClasses.length == 0) {
            return iteratorEntities();
        }
        TIntList idList = new TIntArrayList();
        TIntObjectIterator<? extends Component> primeIterator = store.componentIterator(componentClasses[0]);
        if (primeIterator == null) {
            return NullIterator.newInstance();
        }

        while (primeIterator.hasNext()) {
            primeIterator.advance();
            int id = primeIterator.key();
            boolean discard = false;
            for (int i = 1; i < componentClasses.length; ++i) {
                if (store.get(id, componentClasses[i]) == null) {
                    discard = true;
                    break;
                }
            }
            if (!discard) {
                idList.add(primeIterator.key());
            }
        }
        return new EntityIterable(idList);
    }

    boolean hasComponent(int entityId, Class<? extends Component> componentClass) {
        return store.get(entityId, componentClass) != null;
    }

    Iterable<Component> iterateComponents(int entityId) {
        return store.iterateComponents(entityId);
    }

    void destroy(int entityId) {
        EntityRef ref = createEntityRef(entityId);
        if (eventSystem != null) {
            eventSystem.send(ref, RemovedComponentEvent.newInstance());
        }
        entityCache.remove(ref);
        freedIds.add(entityId);
        if (ref instanceof PojoEntityRef) {
            ((PojoEntityRef)ref).invalidate();
        }
        store.remove(entityId);
    }

    <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        return store.get(entityId, componentClass);
    }

    <T extends Component> T addComponent(int entityId, T component) {
        Component oldComponent = store.put(entityId, component);
        if (eventSystem != null) {
            if (oldComponent == null) {
                eventSystem.send(createEntityRef(entityId), AddComponentEvent.newInstance(), component);
            } else {
                eventSystem.send(createEntityRef(entityId), ChangedComponentEvent.newInstance(), component);
            }
        }
        return component;
    }

    void removeComponent(int entityId, Class<? extends Component> componentClass) {
        Component component = store.get(entityId, componentClass);
        if (component != null) {
            if (eventSystem != null) {
                eventSystem.send(createEntityRef(entityId), RemovedComponentEvent.newInstance(), component);
            }
            store.remove(entityId, componentClass);
        }
    }

    void saveComponent(int entityId, Component component) {
        if (eventSystem != null) {
            eventSystem.send(createEntityRef(entityId), ChangedComponentEvent.newInstance(), component);
        }
    }

    public EntityRef createEntityRefWithId(int id) {
        if (!freedIds.contains(id)) {
            return createEntityRef(id);
        }
        return EntityRef.NULL;
    }
	
    private EntityRef createEntityRef(int entityId) {
        if (entityId == NULL_ID) {
            return EntityRef.NULL;
        }
        PojoEntityRef newRef = new PojoEntityRef(this, entityId);
        PojoEntityRef existing = entityCache.get(newRef);
        if (existing != null) {
            return existing;
        }
        entityCache.put(newRef, newRef);
        return newRef;
    }

    public int getNextId() {
        return nextEntityId;
    }

    public void setNextId(int id) {
        nextEntityId = id;
    }

    public TIntList getFreedIds() {
        return freedIds;
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

    private class EntityIterable implements Iterable<EntityRef>
    {
        private TIntList list;

        public EntityIterable(TIntList list) {
            this.list = list;
        }

        public Iterator<EntityRef> iterator() {
            return new EntityIterator(list.iterator());
        }
    }
    
    private class EntityIterator implements Iterator<EntityRef>
    {
        private TIntIterator idIterator;
        
        public EntityIterator(TIntIterator idIterator) {
            this.idIterator = idIterator;
        }

        public boolean hasNext() {
            return idIterator.hasNext();
        }

        public EntityRef next() {
            return createEntityRef(idIterator.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
