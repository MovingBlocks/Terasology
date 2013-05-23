/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.entitySystem.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedEvent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedEvent;
import org.terasology.entitySystem.lifecycleEvents.OnDeactivatedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityBuilder;
import org.terasology.entitySystem.EntityChangeSubscriber;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.EventSystem;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prototype entity manager. Not intended for final use, but a stand in for experimentation.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityManager implements EntityManager, EngineEntityManager {
    public static final int NULL_ID = 0;

    private static final Logger logger = LoggerFactory.getLogger(PojoEntityManager.class);

    private int nextEntityId = 1;
    private TIntSet freedIds = new TIntHashSet();
    private Map<Integer, EntityRef> entityCache = new MapMaker().weakValues().concurrencyLevel(4).initialCapacity(1000).makeMap();
    private Set<EntityChangeSubscriber> subscribers = Sets.newLinkedHashSet();

    private ComponentTable store = new ComponentTable();
    private EventSystem eventSystem;
    private PrefabManager prefabManager;
    private EntitySystemLibrary entitySystemLibrary;
    private ComponentLibrary componentLibrary;

    private Set<EntityRef> newlyCreated = Sets.newHashSet();
    private SetMultimap<EntityRef, Class<? extends Component>> addedComponents = HashMultimap.create();


    public PojoEntityManager() {
    }

    public void setEntitySystemLibrary(EntitySystemLibrary entitySystemLibrary) {
        this.entitySystemLibrary = entitySystemLibrary;
        componentLibrary = entitySystemLibrary.getComponentLibrary();
    }

    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

    @Override
    public void clear() {
        store.clear();
        nextEntityId = 1;
        freedIds.clear();
        entityCache.clear();
    }

    @Override
    public EntityBuilder newBuilder() {
        return new EntityBuilder(this);
    }

    @Override
    public EntityBuilder newBuilder(String prefabName) {
        if (prefabName != null && !prefabName.isEmpty()) {
            Prefab prefab = prefabManager.getPrefab(prefabName);
            if (prefab == null) {
                logger.warn("Unable to instantiate unknown prefab: \"{}\"", prefabName);
                return new EntityBuilder(this);
            }
            return newBuilder(prefab);
        }
        return newBuilder();
    }

    @Override
    public EntityBuilder newBuilder(Prefab prefab) {
        EntityBuilder builder = new EntityBuilder(this);
        for (Component component : prefab.iterateComponents()) {
            builder.addComponent(componentLibrary.copy(component));
        }
        builder.addComponent(new EntityInfoComponent(prefab.getName(), prefab.isPersisted()));
        return builder;
    }

    @Override
    public EntityRef create() {
        if (!freedIds.isEmpty()) {
            TIntIterator iterator = freedIds.iterator();
            int id = iterator.next();
            iterator.remove();
            return createEntityRef(id);
        }
        if (nextEntityId == NULL_ID) nextEntityId++;
        return createEntityRef(nextEntityId++);
    }

    @Override
    public EntityRef create(Component... components) {
        return create(Arrays.asList(components));
    }

    @Override
    public EntityRef create(Iterable<Component> components) {
        EntityRef entity = create();
        for (Component c : components) {
            store.put(entity.getId(), c);
        }
        if (eventSystem != null) {
            eventSystem.send(entity, OnActivatedEvent.newInstance());
        }
        return entity;
    }

    @Override
    public EntityRef create(String prefabName) {
        if (prefabName != null && !prefabName.isEmpty()) {
            Prefab prefab = prefabManager.getPrefab(prefabName);
            if (prefab == null) {
                logger.warn("Unable to instantiate unknown prefab: \"{}\"", prefabName);
                return EntityRef.NULL;
            }
            return create(prefab);
        }
        return create();
    }

    @Override
    public EntityRef create(String prefabName, Vector3f position) {
        if (prefabName != null && !prefabName.isEmpty()) {
            Prefab prefab = prefabManager.getPrefab(prefabName);
            return create(prefab, position);
        }
        return create();
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation) {
        List<Component> components = Lists.newArrayList();
        for (Component component : prefab.iterateComponents()) {
            Component newComp = componentLibrary.copy(component);
            components.add(newComp);
            if (newComp instanceof LocationComponent) {
                LocationComponent loc = (LocationComponent) newComp;
                loc.setWorldPosition(position);
                loc.setWorldRotation(rotation);
            }
        }
        components.add(new EntityInfoComponent(prefab.getName(), prefab.isPersisted()));
        return create(components);
    }

    @Override
    public EntityRef getEntity(int id) {
        return createEntityRef(id);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position) {
        List<Component> components = Lists.newArrayList();
        for (Component component : prefab.iterateComponents()) {
            Component newComp = componentLibrary.copy(component);
            components.add(newComp);
            if (newComp instanceof LocationComponent) {
                LocationComponent loc = (LocationComponent) newComp;
                loc.setWorldPosition(position);
            }
        }
        components.add(new EntityInfoComponent(prefab.getName(), prefab.isPersisted()));
        return create(components);
    }

    @Override
    public EntityRef create(Prefab prefab) {
        List<Component> components = Lists.newArrayList();
        for (Component component : prefab.iterateComponents()) {
            components.add(componentLibrary.copy(component));
        }
        components.add(new EntityInfoComponent(prefab.getName(), prefab.isPersisted()));
        return create(components);
    }

    @Override
    public EntityRef copy(EntityRef other) {
        if (!other.exists()) {
            return EntityRef.NULL;
        }
        List<Component> newEntityComponents = Lists.newArrayList();
        for (Component c : other.iterateComponents()) {
            newEntityComponents.add(componentLibrary.copy(c));
        }
        return create(newEntityComponents);
    }

    @Override
    public Map<Class<? extends Component>, Component> copyComponents(EntityRef other) {
        Map<Class<? extends Component>, Component> result = Maps.newHashMap();
        for (Component c : other.iterateComponents()) {
            result.put(c.getClass(), componentLibrary.copy(c));
        }
        return result;
    }

    @Override
    public void subscribe(EntityChangeSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(EntityChangeSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public int getComponentCount(Class<? extends Component> componentClass) {
        return store.getComponentCount(componentClass);
    }

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
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
    public <T extends Component> Iterable<Map.Entry<EntityRef, T>> listComponents(Class<T> componentClass) {
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

    public Iterable<EntityRef> listEntities() {
        return new Iterable<EntityRef>() {
            public Iterator<EntityRef> iterator() {
                return new EntityIterator(store.entityIdIterator());
            }
        };
    }

    public Iterable<EntityRef> listEntitiesWith(Class<? extends Component>... componentClasses) {
        if (componentClasses.length == 0) {
            return listEntities();
        }
        if (componentClasses.length == 1) {
            return iterateEntities(componentClasses[0]);
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

    private Iterable<EntityRef> iterateEntities(Class<? extends Component> componentClass) {
        TIntList idList = new TIntArrayList();
        TIntObjectIterator<? extends Component> primeIterator = store.componentIterator(componentClass);
        if (primeIterator == null) {
            return NullIterator.newInstance();
        }

        while (primeIterator.hasNext()) {
            primeIterator.advance();
            int id = primeIterator.key();
            idList.add(primeIterator.key());
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
            eventSystem.send(ref, OnDeactivatedEvent.newInstance());
        }
        entityCache.remove(entityId);
        freedIds.add(entityId);
        if (ref instanceof PojoEntityRef) {
            ((PojoEntityRef) ref).invalidate();
        }
        store.remove(entityId);
    }

    @Override
    public void removedForStoring(EntityRef entity) {
        if (entity.exists()) {
            int entityId = entity.getId();
            if (eventSystem != null) {
                eventSystem.send(entity, OnDeactivatedEvent.newInstance());
            }
            store.remove(entityId);
        }
    }

    <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        //return componentLibrary.copy(store.get(entityId, componentClass));
        return store.get(entityId, componentClass);
    }

    <T extends Component> T addComponent(int entityId, T component) {
        Component oldComponent = store.put(entityId, component);
        if (oldComponent != null) {
            logger.error("Adding a component ({}) over an existing component for entity {}", component.getClass(), entityId);
        }
        if (eventSystem != null) {
            if (oldComponent == null) {
                eventSystem.send(createEntityRef(entityId), OnActivatedEvent.newInstance(), component);
            } else {
                eventSystem.send(createEntityRef(entityId), OnChangedEvent.newInstance(), component);
            }
        }
        if (oldComponent == null) {
            notifyComponentAdded(getEntity(entityId), component.getClass());
        } else {
            notifyComponentChanged(getEntity(entityId), component.getClass());
        }
        return component;
    }

    void removeComponent(int entityId, Class<? extends Component> componentClass) {
        Component component = store.get(entityId, componentClass);
        if (component != null) {
            if (eventSystem != null) {
                eventSystem.send(createEntityRef(entityId), OnDeactivatedEvent.newInstance(), component);
            }
            store.remove(entityId, componentClass);
            notifyComponentRemoved(getEntity(entityId), componentClass);
        }
    }

    void saveComponent(int entityId, Component component) {
        Component oldComponent = store.put(entityId, component);
        if (oldComponent == null) {
            logger.error("Saving a component ({}) that doesn't belong to this entity {}", component.getClass(), entityId);
        }
        if (eventSystem != null) {
            if (oldComponent == null) {
                eventSystem.send(createEntityRef(entityId), OnActivatedEvent.newInstance(), component);
            } else {
                eventSystem.send(createEntityRef(entityId), OnChangedEvent.newInstance(), component);
            }
        }
        if (oldComponent == null) {
            notifyComponentAdded(getEntity(entityId), component.getClass());
        } else {
            notifyComponentChanged(getEntity(entityId), component.getClass());
        }
    }

    @Override
    public EntityRef createEntityRefWithId(int id) {
        if (!freedIds.contains(id)) {
            return createEntityRef(id);
        }
        return EntityRef.NULL;
    }

    @Override
    public EntityRef createEntityWithId(int id, Iterable<Component> components) {
        if (!freedIds.contains(id)) {
            EntityRef entity = createEntityRef(id);
            for (Component c : components) {
                store.put(id, c);
            }
            if (eventSystem != null) {
                eventSystem.send(entity, OnActivatedEvent.newInstance());
            }
            return entity;
        }
        return EntityRef.NULL;
    }

    private EntityRef createEntityRef(int entityId) {
        if (entityId == NULL_ID) {
            return EntityRef.NULL;
        }
        EntityRef existing = entityCache.get(entityId);
        if (existing != null) {
            return existing;
        }
        PojoEntityRef newRef = new PojoEntityRef(this, entityId);
        entityCache.put(entityId, newRef);
        return newRef;
    }

    @Override
    public int getNextId() {
        return nextEntityId;
    }

    @Override
    public void setNextId(int id) {
        nextEntityId = id;
    }

    @Override
    public TIntSet getFreedIds() {
        return freedIds;
    }

    private void notifyComponentAdded(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentAdded(changedEntity, component);
        }
    }

    private void notifyComponentRemoved(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentRemoved(changedEntity, component);
        }
    }

    private void notifyComponentChanged(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentChange(changedEntity, component);
        }
    }

    public int getActiveEntities() {
        return entityCache.size();
    }

    private static class EntityEntry<T> implements Map.Entry<EntityRef, T> {
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

    private class EntityIterable implements Iterable<EntityRef> {
        private TIntList list;

        public EntityIterable(TIntList list) {
            this.list = list;
        }

        public Iterator<EntityRef> iterator() {
            return new EntityIterator(list.iterator());
        }
    }

    private class EntityIterator implements Iterator<EntityRef> {
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
