package org.terasology.entitySystem.pojo;

import com.google.common.collect.Lists;
import com.google.protobuf.TextFormat;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.entitySystem.pojo.persistence.*;
import org.terasology.entitySystem.pojo.persistence.extension.EntityRefTypeHandler;
import org.terasology.protobuf.EntityData;

import java.io.*;
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
    private EntityPersister entityPersister;

    // Temporary list of valid ids used during loading.
    // TODO: Store in EntityRefTypeHandler?
    private TIntSet validIds = new TIntHashSet();

    public PojoEntityManager() {
        entityPersister = new EntityPersisterImpl();
        entityPersister.setPersistableEntityManager(this);
    }

    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        entityPersister.registerTypeHandler(forClass, handler);
    }

    public void registerComponentClass(Class<? extends Component> componentClass) {
        entityPersister.registerComponentClass(componentClass);
    }

    public void save(File file, SaveFormat format) throws IOException {
        final EntityData.World world = entityPersister.serializeWorld();

        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
        try {
            switch (format) {
                case Binary:
                    world.writeTo(out);
                    out.flush();
                    break;
                case Text:
                    TextFormat.print(world, bufferedWriter);
                    bufferedWriter.flush();
                    break;
                case JSON:
                    EntityDataJSONFormat.write(world, bufferedWriter);
                    bufferedWriter.flush();
                    break;
            }
        }
        finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close file", e);
            }
        }
    }

    public void load(File file, SaveFormat format) throws IOException {
        clear();

        FileInputStream in = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        EntityData.World world = null;
        try {
            switch (format) {
                case Binary:
                    world = EntityData.World.parseFrom(in);
                    break;
                case Text:
                    EntityData.World.Builder builder = EntityData.World.newBuilder();
                    TextFormat.merge(bufferedReader, builder);
                    world = builder.build();
                    break;
                case JSON:
                    throw new IOException("Reading JSON format not supported yet");
            }
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close file", e);
            }
        }

        if (world != null) {
            entityPersister.deserializeWorld(world);
        }
    }

    public void clear() {
        store.clear();
        nextEntityId = 1;
        freedIds.clear();
        entityCache.clear();
    }

    public EntityRef create() {
        if (!freedIds.isEmpty()) {
            createEntityRef(freedIds.removeAt(freedIds.size() - 1));
        }
        if (nextEntityId == NULL_ID) nextEntityId++;
        return createEntityRef(nextEntityId++);
    }

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

    public EntityRef create(Prefab prefab) {
        EntityRef result = create();
        if (prefab != null) {
            for (Component component : prefab.listComponents()) {
                result.addComponent(entityPersister.copyComponent(component));
            }
            result.addComponent(new EntityInfoComponent(prefab.getName()));
        }
        return result;
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

    public int getComponentCount(Class<? extends Component> componentClass) {
        return store.getComponentCount(componentClass);
    }

    public EventSystem getEventSystem() {
        return eventSystem;
    }

    public void setEventSystem(EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    public PrefabManager getPrefabManager() {
        return prefabManager;
    }

    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
        entityPersister.setPrefabManager(prefabManager);
    }

    public EntityPersister getPersister() {
        return entityPersister;
    }

    // Used for testing, for now
    public EntityPersister getEntityPersister() {
        return entityPersister;
    }

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
        return createEntityRef(id);
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
