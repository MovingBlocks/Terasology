/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.entitySystem.entity.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.UnsignedInts;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeEntityCreated;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prototype entity manager. Not intended for final use, but a stand in for experimentation.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityManager implements LowLevelEntityManager, EngineEntityManager {
    public static final int NULL_ID = 0;

    private static final Logger logger = LoggerFactory.getLogger(PojoEntityManager.class);

    private int nextEntityId = 1;
    private TIntSet loadedIds = new TIntHashSet();
    private TIntSet freedIds = new TIntHashSet();
    private Map<Integer, BaseEntityRef> entityCache = new MapMaker().weakValues().concurrencyLevel(4).initialCapacity(1000).makeMap();
    private ComponentTable store = new ComponentTable();

    private Set<EntityChangeSubscriber> subscribers = Sets.newLinkedHashSet();
    private Set<EntityDestroySubscriber> destroySubscribers = Sets.newLinkedHashSet();
    private EventSystem eventSystem;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;

    private RefStrategy refStrategy = new DefaultRefStrategy();

    private TypeSerializationLibrary typeSerializerLibrary;

    public PojoEntityManager() {
    }

    public void setTypeSerializerLibrary(TypeSerializationLibrary serializerLibrary) {
        this.typeSerializerLibrary = serializerLibrary;
    }

    public void setEntitySystemLibrary(EntitySystemLibrary entitySystemLibrary) {
        componentLibrary = entitySystemLibrary.getComponentLibrary();
    }

    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

    @Override
    public void clear() {
        for (BaseEntityRef entityRef : entityCache.values()) {
            entityRef.invalidate();
        }
        store.clear();
        nextEntityId = 1;
        loadedIds.clear();
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
        if (prefab != null) {
            for (Component component : prefab.iterateComponents()) {
                builder.addComponent(componentLibrary.copy(component));
            }
            builder.addComponent(new EntityInfoComponent(prefab.getName(), prefab.isPersisted(), prefab.isAlwaysRelevant()));
        }
        return builder;
    }

    @Override
    public EntityRef create() {
        return createEntityRef(createEntity());
    }

    private int createEntity() {
        if (!freedIds.isEmpty()) {
            TIntIterator iterator = freedIds.iterator();
            int id = iterator.next();
            iterator.remove();
            loadedIds.add(id);
            return id;
        }
        if (nextEntityId == NULL_ID) {
            nextEntityId++;
        }
        loadedIds.add(nextEntityId);
        return nextEntityId++;
    }

    @Override
    public EntityRef create(Component... components) {
        return create(Arrays.asList(components));
    }

    @Override
    public EntityRef create(Iterable<Component> components) {
        EntityRef entity = createEntity(components);
        if (eventSystem != null) {
            eventSystem.send(entity, OnAddedComponent.newInstance());
            eventSystem.send(entity, OnActivatedComponent.newInstance());
        }
        return entity;
    }

    public void setEntityRefStrategy(RefStrategy strategy) {
        this.refStrategy = strategy;
    }

    private EntityRef createEntity(Iterable<Component> components) {
        int entityId = createEntity();

        Prefab prefab = null;
        for (Component component : components) {
            if (component instanceof EntityInfoComponent) {
                EntityInfoComponent comp = (EntityInfoComponent) component;
                prefab = prefabManager.getPrefab(comp.parentPrefab);
                break;
            }
        }

        Iterable<Component> finalComponents;
        if (eventSystem != null) {
            BeforeEntityCreated event = new BeforeEntityCreated(prefab, components);
            BaseEntityRef tempRef = refStrategy.createRefFor(entityId, this);
            eventSystem.send(tempRef, event);
            tempRef.invalidate();
            finalComponents = event.getResultComponents();
        } else {
            finalComponents = components;
        }

        for (Component c : finalComponents) {
            store.put(entityId, c);
        }
        return createEntityRef(entityId);
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
        components.add(new EntityInfoComponent(prefab.getName(), prefab.isPersisted(), prefab.isAlwaysRelevant()));
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
        components.add(new EntityInfoComponent(prefab.getName(), prefab.isPersisted(), prefab.isAlwaysRelevant()));
        return create(components);
    }

    @Override
    public EntityRef create(Prefab prefab) {
        List<Component> components = Lists.newArrayList();
        for (Component component : prefab.iterateComponents()) {
            components.add(componentLibrary.copy(component));
        }
        components.add(new EntityInfoComponent(prefab.getName(), prefab.isPersisted(), prefab.isAlwaysRelevant()));
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
    public Iterable<EntityRef> getAllEntities() {
        return new Iterable<EntityRef>() {
            public Iterator<EntityRef> iterator() {
                return new EntityIterator(store.entityIdIterator());
            }
        };
    }

    @SafeVarargs
    @Override
    public final Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses) {
        if (componentClasses.length == 0) {
            return getAllEntities();
        }
        if (componentClasses.length == 1) {
            return iterateEntities(componentClasses[0]);
        }
        TIntList idList = new TIntArrayList();
        TIntObjectIterator<? extends Component> primeIterator = store.componentIterator(componentClasses[0]);
        if (primeIterator == null) {
            return Collections.emptyList();
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
            return Collections.emptyList();
        }

        while (primeIterator.hasNext()) {
            primeIterator.advance();
            int id = primeIterator.key();
            idList.add(primeIterator.key());
        }
        return new EntityIterable(idList);
    }

    @Override
    public int getActiveEntityCount() {
        return entityCache.size();
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
    public PrefabManager getPrefabManager() {
        return prefabManager;
    }


    /*
     * Engine features
     */

    @Override
    public EntityRef createEntityRefWithId(int id) {
        if (isExistingEntity(id)) {
            return createEntityRef(id);
        }
        return EntityRef.NULL;
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components) {
        return createEntity(components);
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(String prefabName) {
        return createEntityWithoutLifecycleEvents(getPrefabManager().getPrefab(prefabName));
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Prefab prefab) {
        if (prefab != null) {
            List<Component> components = Lists.newArrayList();
            for (Component component : prefab.iterateComponents()) {
                components.add(componentLibrary.copy(component));
            }
            components.add(new EntityInfoComponent(prefab.getName(), prefab.isPersisted(), prefab.isAlwaysRelevant()));

            return createEntityWithoutLifecycleEvents(components);
        } else {
            return createEntityWithoutLifecycleEvents(Collections.<Component>emptyList());
        }
    }

    @Override
    public void destroyEntityWithoutEvents(EntityRef entity) {
        if (entity.isActive()) {
            destroy(entity);
        }
    }

    @Override
    public EntityRef createEntityWithId(int id, Iterable<Component> components) {
        if (!freedIds.contains(id)) {
            for (Component c : components) {
                store.put(id, c);
            }
            loadedIds.add(id);
            EntityRef entity = createEntityRef(id);
            if (eventSystem != null) {
                eventSystem.send(entity, OnActivatedComponent.newInstance());
            }
            return entity;
        }
        return EntityRef.NULL;
    }

    @Override
    public void subscribe(EntityChangeSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void subscribe(EntityDestroySubscriber subscriber) {
        destroySubscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(EntityChangeSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void setEventSystem(EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    @Override
    public TypeSerializationLibrary getTypeSerializerLibrary() {
        return typeSerializerLibrary;
    }

    @Override
    public void deactivateForStorage(EntityRef entity) {
        if (entity.exists()) {
            int entityId = entity.getId();
            if (eventSystem != null) {
                eventSystem.send(entity, BeforeDeactivateComponent.newInstance());
            }
            loadedIds.remove(entityId);
            store.remove(entityId);
        }
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

    /*
     * For use by Entity Refs
     */

    /**
     * @param entityId
     * @param componentClass
     * @return Whether the entity has a component of the given type
     */
    @Override
    public boolean hasComponent(int entityId, Class<? extends Component> componentClass) {
        return store.get(entityId, componentClass) != null;
    }

    @Override
    public boolean isExistingEntity(int id) {
        return freedIds.contains(id) || UnsignedInts.toLong(nextEntityId) > UnsignedInts.toLong(id);
    }

    /**
     * @param id
     * @return Whether the entity is currently active
     */
    @Override
    public boolean isActiveEntity(int id) {
        return loadedIds.contains(id);
    }

    /**
     * @param entityId
     * @return An iterable over the components of the given entity
     */
    @Override
    public Iterable<Component> iterateComponents(int entityId) {
        return store.iterateComponents(entityId);
    }

    /**
     * Destroys this entity, sending event
     *
     * @param entityId
     */
    @Override
    public void destroy(int entityId) {
        // Don't allow the destruction of unloaded entities.
        if (!loadedIds.contains(entityId)) {
            return;
        }
        EntityRef ref = createEntityRef(entityId);
        if (eventSystem != null) {
            eventSystem.send(ref, BeforeDeactivateComponent.newInstance());
            eventSystem.send(ref, BeforeRemoveComponent.newInstance());
        }
        for (Component comp : store.iterateComponents(entityId)) {
            notifyComponentRemoved(ref, comp.getClass());
        }
        destroy(ref);
        for (EntityDestroySubscriber destroySubscriber : destroySubscribers) {
            destroySubscriber.onEntityDestroyed(entityId);
        }
    }

    private void destroy(EntityRef ref) {
        // Don't allow the destruction of unloaded entities.
        int entityId = ref.getId();
        entityCache.remove(entityId);
        loadedIds.remove(entityId);
        freedIds.add(entityId);
        if (ref instanceof PojoEntityRef) {
            ((PojoEntityRef) ref).invalidate();
        }
        store.remove(entityId);
    }

    /**
     * @param entityId
     * @param componentClass
     * @param <T>
     * @return The component of that type owned by the given entity, or null if it doesn't have that component
     */
    @Override
    public <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        //return componentLibrary.copy(store.get(entityId, componentClass));
        return store.get(entityId, componentClass);
    }

    /**
     * Adds (or replaces) a component to an entity
     *
     * @param entityId
     * @param component
     * @param <T>
     * @return The added component
     */
    @Override
    public <T extends Component> T addComponent(int entityId, T component) {
        Component oldComponent = store.put(entityId, component);
        if (oldComponent != null) {
            logger.error("Adding a component ({}) over an existing component for entity {}", component.getClass(), entityId);
        }
        if (eventSystem != null) {
            EntityRef entityRef = createEntityRef(entityId);
            if (oldComponent == null) {
                eventSystem.send(entityRef, OnAddedComponent.newInstance(), component);
                eventSystem.send(entityRef, OnActivatedComponent.newInstance(), component);
            } else {
                eventSystem.send(entityRef, OnChangedComponent.newInstance(), component);
            }
        }
        if (oldComponent == null) {
            notifyComponentAdded(getEntity(entityId), component.getClass());
        } else {
            notifyComponentChanged(getEntity(entityId), component.getClass());
        }
        return component;
    }

    /**
     * Removes a component from an entity
     *
     * @param entityId
     * @param componentClass
     */
    @Override
    public <T extends Component> T removeComponent(int entityId, Class<T> componentClass) {
        T component = store.get(entityId, componentClass);
        if (component != null) {
            if (eventSystem != null) {
                EntityRef entityRef = createEntityRef(entityId);
                eventSystem.send(entityRef, BeforeDeactivateComponent.newInstance(), component);
                eventSystem.send(entityRef, BeforeRemoveComponent.newInstance(), component);
            }
            notifyComponentRemoved(getEntity(entityId), componentClass);
            store.remove(entityId, componentClass);
        }
        return component;
    }

    /**
     * Saves a component to an entity
     *
     * @param entityId
     * @param component
     */
    @Override
    public void saveComponent(int entityId, Component component) {
        Component oldComponent = store.put(entityId, component);
        if (oldComponent == null) {
            logger.error("Saving a component ({}) that doesn't belong to this entity {}", component.getClass(), entityId);
        }
        if (eventSystem != null) {
            EntityRef entityRef = createEntityRef(entityId);
            if (oldComponent == null) {
                eventSystem.send(entityRef, OnAddedComponent.newInstance(), component);
                eventSystem.send(entityRef, OnActivatedComponent.newInstance(), component);
            } else {
                eventSystem.send(entityRef, OnChangedComponent.newInstance(), component);
            }
        }
        if (oldComponent == null) {
            notifyComponentAdded(getEntity(entityId), component.getClass());
        } else {
            notifyComponentChanged(getEntity(entityId), component.getClass());
        }
    }

    /*
     * Implementation
     */

    private EntityRef createEntityRef(int entityId) {
        if (entityId == NULL_ID) {
            return EntityRef.NULL;
        }
        BaseEntityRef existing = entityCache.get(entityId);
        if (existing != null) {
            return existing;
        }
        BaseEntityRef newRef = refStrategy.createRefFor(entityId, this);
        entityCache.put(entityId, newRef);
        return newRef;
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

    // For testing
    @Override
    @SafeVarargs
    public final int getCountOfEntitiesWith(Class<? extends Component>... componentClasses) {
        switch (componentClasses.length) {
            case 0:
                return store.numEntities();
            case 1:
                return store.getComponentCount(componentClasses[0]);
            default:
                return Lists.newArrayList(getEntitiesWith(componentClasses)).size();
        }
    }

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
        return Collections.emptyList();
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

        public T setValue(T newValue) {
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
