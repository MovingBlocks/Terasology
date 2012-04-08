package org.terasology.entitySystem.pojo;

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
public class PojoEntityManager implements EntityManager {
    public static final int NULL_ID = 0;

    private static Logger logger = Logger.getLogger(PojoEntityManager.class.getName());

    private int nextEntityId = 1;
    private TIntList freedIds = new TIntArrayList();
    private Map<EntityRef, PojoEntityRef> entityCache = new WeakHashMap<EntityRef, PojoEntityRef>();

    ComponentTable store = new ComponentTable();
    private EventSystem eventSystem;
    private PrefabManager prefabManager;
    private EntityPersister entityPersister = new EntityPersisterImpl();

    // Temporary list of valid ids used during loading.
    // TODO: Store in EntityRefTypeHandler?
    private TIntSet validIds = new TIntHashSet();

    public PojoEntityManager() {
        registerTypeHandler(EntityRef.class, new EntityRefTypeHandler(this));
        registerComponentClass(EntityInfoComponent.class);
    }

    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        entityPersister.registerTypeHandler(forClass, handler);

    }

    public void registerComponentClass(Class<? extends Component> componentClass) {
        entityPersister.registerComponentClass(componentClass);
    }

    public EntityRef loadEntityRef(int id) {
        if (!validIds.contains(id)) {
            return EntityRef.NULL;
        }
        return createEntityRef(id);
    }

    public void save(File file, SaveFormat format) throws IOException {
        final EntityData.World.Builder world = serializeWorld();

        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
        try {
            switch (format) {
                case Binary:
                    // TODO: Do we need to buffer this output stream, or is it buffered in google's code?
                    world.build().writeTo(out);
                    out.flush();
                    break;
                case Text:
                    TextFormat.print(world.build(), bufferedWriter);
                    bufferedWriter.flush();
                    break;
                case JSON:
                    EntityDataJSONFormat.write(bufferedWriter, world.build());
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

    private EntityData.World.Builder serializeWorld() {
        final EntityData.World.Builder world = EntityData.World.newBuilder();
        world.setNextEntityId(nextEntityId);
        freedIds.forEach(new TIntProcedure() {
            public boolean execute(int i) {
                world.addFreedEntityId(i);
                return true;
            }
        });

        for (Prefab prefab : prefabManager.listPrefabs()) {
            EntityData.Prefab.Builder prefabData = EntityData.Prefab.newBuilder();
            prefabData.setName(prefab.getName());
            for (Prefab parent : prefab.getParents()) {
                prefabData.addParentName(parent.getName());
            }

            for (Component component : prefab.listOwnComponents()) {
                EntityData.Component componentData = entityPersister.serializeComponent(component);
                if (componentData != null) {
                    prefabData.addComponent(componentData);
                }
            }
            world.addPrefab(prefabData.build());
        }

        TIntIterator idIterator = store.entityIdIterator();
        while (idIterator.hasNext()) {
            int id = idIterator.next();
            EntityInfoComponent entityInfo = getComponent(id, EntityInfoComponent.class);
            if (entityInfo != null && prefabManager.exists(entityInfo.parentPrefab)) {
                world.addEntity(entityPersister.serializeEntity(id, createEntityRef(id), prefabManager.getPrefab(entityInfo.parentPrefab)));
            } else {
                world.addEntity(entityPersister.serializeEntity(id, createEntityRef(id)));
            }
        }
        return world;
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
            deserializeWorld(world);
        }
    }

    private void deserializeWorld(EntityData.World world) {
        nextEntityId = world.getNextEntityId();
        for (Integer deadId : world.getFreedEntityIdList()) {
            freedIds.add(deadId);
        }

        for (EntityData.Prefab prefabData : world.getPrefabList()) {
            if (!prefabManager.exists(prefabData.getName())) {
                Prefab prefab = prefabManager.createPrefab(prefabData.getName());
                for (String parentName : prefabData.getParentNameList()) {
                    Prefab parent = prefabManager.getPrefab(parentName);
                    if (parent == null) {
                        logger.log(Level.SEVERE, "Missing parent prefab (need to fix parent serialization)");
                    } else {
                        prefab.addParent(parent);
                    }
                }
                for (EntityData.Component componentData : prefabData.getComponentList()) {
                    Component component = entityPersister.deserializeComponent(componentData);
                    if (component != null) {
                        prefab.setComponent(component);
                    }
                }
            }
        }

        // Gather valid ids
        validIds = new TIntHashSet(world.getEntityCount());
        for (EntityData.Entity entityData : world.getEntityList()) {
            validIds.add(entityData.getId());
        }

        for (EntityData.Entity entityData : world.getEntityList()) {
            int entityId = entityData.getId();
            for (EntityData.Component componentData : entityData.getComponentList()) {
                Component component = entityPersister.deserializeComponent(componentData);
                if (component != null) {
                    store.put(entityId, component);
                }
            }
        }

        validIds = new TIntHashSet();
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

    public Iterable<EntityRef> iteratorEntities(Class<? extends Component>... componentClasses) {
        if (componentClasses.length == 0) {
            return NullIterator.newInstance();
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
