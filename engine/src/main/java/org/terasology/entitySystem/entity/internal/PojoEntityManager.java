/*
 * Copyright 2017 MovingBlocks
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityCache;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeEntityCreated;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class PojoEntityManager implements EngineEntityManager {
    public static final long NULL_ID = 0;

    private static final Logger logger = LoggerFactory.getLogger(PojoEntityManager.class);

    private long nextEntityId = 1;
    private TLongSet loadedIds = new TLongHashSet();

    private PojoEntityCache globalCache = new PojoEntityCache(this);
    private PojoEntityCache sectorCache = new PojoEntityCache(this);

    private Set<EntityChangeSubscriber> subscribers = Sets.newLinkedHashSet();
    private Set<EntityDestroySubscriber> destroySubscribers = Sets.newLinkedHashSet();
    private EventSystem eventSystem;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;

    private RefStrategy refStrategy = new DefaultRefStrategy();

    private TypeSerializationLibrary typeSerializerLibrary;

    public PojoEntityManager() {
    }

    public boolean registerId(long entityId) {
        if (entityId >= nextEntityId) {
            logger.error("Prevented attempt to create entity with an invalid id.");
            return false;
        }
        loadedIds.add(entityId);
        return true;
    }

    public boolean idLoaded(long entityId) {
        return loadedIds.contains(entityId);
    }

    public void remove(long entityId) {
        loadedIds.remove(entityId);
    }

    public void setTypeSerializerLibrary(TypeSerializationLibrary serializerLibrary) {
        this.typeSerializerLibrary = serializerLibrary;
    }

    public void setComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
    }

    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

    @Override
    public void clear() {
        //Todo: implement clear in the stores
        globalCache.clear();
        sectorCache.clear();
        nextEntityId = 1;
        loadedIds.clear();
    }

    public EntityRef createEntityRefWithId(long entityId) {
        return globalCache.createEntityRefWithId(entityId);
    }

    private EntityRef createEntity(Iterable<Component> components) {
        long entityId = createEntity();

        Prefab prefab = null;
        for (Component component : components) {
            if (component instanceof EntityInfoComponent) {
                EntityInfoComponent comp = (EntityInfoComponent) component;
                prefab = comp.parentPrefab;
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
            globalCache.getComponentStore().put(entityId, c);
        }
        return createEntityRef(entityId);
    }

    @Override
    public void destroyEntityWithoutEvents(EntityRef entity) {
        globalCache.destroyEntityWithoutEvents(entity);
    }

    @Override
    public EntityRef createEntityWithId(long id, Iterable<Component> components) {
        return globalCache.createEntityWithId(id, components);
    }

    @Override
    public void destroy(long entityId) {
        globalCache.destroy(entityId);
    }


    @Override
    public void setEntityRefStrategy(RefStrategy strategy) {
        this.refStrategy = strategy;
    }

    @Override
    public RefStrategy getEntityRefStrategy() {
        return refStrategy;
    }

    @Override
    public EntityRef getEntity(long id) {
        return createEntityRef(id);
    }

    @Override
    //Todo: Depreciated, maybe remove? Not many uses
    public EntityRef copy(EntityRef other) {
        if (!other.exists()) {
            return EntityRef.NULL;
        }
        List<Component> newEntityComponents = Lists.newArrayList();
        for (Component c : other.iterateComponents()) {
            newEntityComponents.add(componentLibrary.copy(c));
        }
        return globalCache.create(newEntityComponents);
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
    //Todo: implement iterating over multiple caches
    public Iterable<EntityRef> getAllEntities() {
        return () -> new EntityIterator(globalCache.getComponentStore().entityIdIterator());
    }

    @SafeVarargs
    @Override
    //Todo: implement iterating over multiple caches
    public final Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses) {
        if (componentClasses.length == 0) {
            return getAllEntities();
        }
        if (componentClasses.length == 1) {
            return iterateEntities(componentClasses[0]);
        }
        TLongList idList = new TLongArrayList();
        TLongObjectIterator<? extends Component> primeIterator = globalCache.getComponentStore().componentIterator(componentClasses[0]);
        if (primeIterator == null) {
            return Collections.emptyList();
        }

        while (primeIterator.hasNext()) {
            primeIterator.advance();
            long id = primeIterator.key();
            boolean discard = false;
            for (int i = 1; i < componentClasses.length; ++i) {
                if (globalCache.getComponentStore().get(id, componentClasses[i]) == null) {
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
        TLongList idList = new TLongArrayList();
        TLongObjectIterator<? extends Component> primeIterator = globalCache.getComponentStore().componentIterator(componentClass);
        if (primeIterator == null) {
            return Collections.emptyList();
        }

        while (primeIterator.hasNext()) {
            primeIterator.advance();
            long id = primeIterator.key();
            idList.add(primeIterator.key());
        }
        return new EntityIterable(idList);
    }

    @Override
    public int getActiveEntityCount() {
        return globalCache.getEntityStore().size() + sectorCache.getEntityStore().size();
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

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components) {
        EntityRef entity = createEntity(components);
        for (Component component: components) {
            notifyComponentAdded(entity, component.getClass());
        }
        return entity;
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(String prefabName) {
        return createEntityWithoutLifecycleEvents(getPrefabManager().getPrefab(prefabName));
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Prefab prefab) {
        if (prefab != null) {
            List<Component> components = Lists.newArrayList();
            for (Component component : prefab.iterateComponents()) {
                components.add(componentLibrary.copy(component));
            }
            components.add(new EntityInfoComponent(prefab, prefab.isPersisted(), prefab.isAlwaysRelevant()));

            return createEntityWithoutLifecycleEvents(components);
        } else {
            return createEntityWithoutLifecycleEvents(Collections.<Component>emptyList());
        }
    }

    public EntityCache getGlobalCache () {
        return globalCache;
    }

    public EntityCache getSectorCache() {
        return sectorCache;
    }

    /*
     * Engine features
     */


    @Override
    public void subscribeForChanges(EntityChangeSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void subscribeForDestruction(EntityDestroySubscriber subscriber) {
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
    //Todo: include sector entities
    public void deactivateForStorage(EntityRef entity) {
        if (entity.exists()) {
            long entityId = entity.getId();
            if (eventSystem != null) {
                eventSystem.send(entity, BeforeDeactivateComponent.newInstance());
            }
            List<Component> components = globalCache.getComponentStore().getComponentsInNewList(entityId);
            components = Collections.unmodifiableList(components);
            notifyBeforeDeactivation(entity, components);
            for (Component component: components) {
                globalCache.getComponentStore().remove(entityId, component.getClass());
            }
            loadedIds.remove(entityId);
        }
    }

    @Override
    public long getNextId() {
        return nextEntityId;
    }

    @Override
    public void setNextId(long id) {
        nextEntityId = id;
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
    public boolean hasComponent(long entityId, Class<? extends Component> componentClass) {
        return (globalCache.getComponentStore().get(entityId, componentClass) != null) ||
               (sectorCache.getComponentStore().get(entityId, componentClass) != null);
    }

    @Override
    public boolean isExistingEntity(long id) {
        return nextEntityId > id;
    }

    /**
     * @param id
     * @return Whether the entity is currently active
     */
    @Override
    public boolean isActiveEntity(long id) {
        return loadedIds.contains(id);
    }

    /**
     * @param entityId
     * @return An iterable over the components of the given entity
     */
    @Override
    //Todo: implement iterating over multiple caches
    public Iterable<Component> iterateComponents(long entityId) {
        return globalCache.getComponentStore().iterateComponents(entityId);
    }

    @Override
    public void notifyComponentRemovalAndEntityDestruction(long entityId, EntityRef ref) {
        for (Component comp : globalCache.getComponentStore().iterateComponents(entityId)) {
            notifyComponentRemoved(ref, comp.getClass());
        }
        for (EntityDestroySubscriber destroySubscriber : destroySubscribers) {
            destroySubscriber.onEntityDestroyed(ref);
        }
    }

    /**
     * @param entityId
     * @param componentClass
     * @param <T>
     * @return The component of that type owned by the given entity, or null if it doesn't have that component
     */
    @Override
    //Todo: check all caches
    public <T extends Component> T getComponent(long entityId, Class<T> componentClass) {
        return globalCache.getComponentStore().get(entityId, componentClass);
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
    //Todo: be able to add to entities in any cache
    public <T extends Component> T addComponent(long entityId, T component) {
        Preconditions.checkNotNull(component);
        Component oldComponent = globalCache.getComponentStore().put(entityId, component);
        if (oldComponent != null) {
            logger.error("Adding a component ({}) over an existing component for entity {}", component.getClass(), entityId);
        }
        if (oldComponent == null) {
            notifyComponentAdded(getEntity(entityId), component.getClass());
        } else {
            notifyComponentChanged(getEntity(entityId), component.getClass());
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
        return component;
    }

    /**
     * Removes a component from an entity
     *
     * @param entityId
     * @param componentClass
     */
    @Override
    //Todo: be able to remove from entities in any cache
    public <T extends Component> T removeComponent(long entityId, Class<T> componentClass) {
        T component = globalCache.getComponentStore().get(entityId, componentClass);
        if (component != null) {
            if (eventSystem != null) {
                EntityRef entityRef = createEntityRef(entityId);
                eventSystem.send(entityRef, BeforeDeactivateComponent.newInstance(), component);
                eventSystem.send(entityRef, BeforeRemoveComponent.newInstance(), component);
            }
            notifyComponentRemoved(getEntity(entityId), componentClass);
            globalCache.getComponentStore().remove(entityId, componentClass);
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
    //Todo: be able to save components for entities in any cache
    public void saveComponent(long entityId, Component component) {
        Component oldComponent = globalCache.getComponentStore().put(entityId, component);
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

    public long createEntity() {
        if (nextEntityId == NULL_ID) {
            nextEntityId++;
        }
        loadedIds.add(nextEntityId);
        return nextEntityId++;
    }

    private EntityRef createEntityRef(long entityId) {
        if (entityId == NULL_ID) {
            return EntityRef.NULL;
        }
        BaseEntityRef existing = globalCache.getEntityStore().get(entityId);
        if (existing != null) {
            return existing;
        }
        BaseEntityRef newRef = refStrategy.createRefFor(entityId, this);
        globalCache.getEntityStore().put(entityId, newRef);
        return newRef;
    }

    public EntityRef createSectorEntity() {
        return sectorCache.create();
    }

    private EntityRef createSectorEntityRef(long entityId) {
        if (entityId == NULL_ID) {
            return EntityRef.NULL;
        }

        //Return existing entity if it exists
        BaseEntityRef globalEntity = globalCache.getEntityStore().get(entityId);
        BaseEntityRef sectorEntity = sectorCache.getEntityStore().get(entityId);
        if(globalEntity != null) {
            return globalEntity;
        } else if (sectorEntity != null) {
            return sectorEntity;
        }

        BaseEntityRef newRef = refStrategy.createRefFor(entityId, this);
        sectorCache.getEntityStore().put(entityId, newRef);
        return newRef;
    }

    public void notifyComponentAdded(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentAdded(changedEntity, component);
        }
    }

    public void notifyComponentRemoved(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentRemoved(changedEntity, component);
        }
    }

    public void notifyComponentChanged(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentChange(changedEntity, component);
        }
    }

    /**
     * This method gets called when the entity gets reactivated. e.g. after storage an entity needs to be reactivated.
     */
    private void notifyReactivation(EntityRef entity, Collection<Component> components) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onReactivation(entity, components);
        }
    }


    /**
     * This method gets called before an entity gets deactivated (e.g. for storage).
     */
    private void notifyBeforeDeactivation(EntityRef entity, Collection<Component> components) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onBeforeDeactivation(entity, components);
        }
    }

    // For testing
    @Override
    @SafeVarargs
    public final int getCountOfEntitiesWith(Class<? extends Component>... componentClasses) {
        switch (componentClasses.length) {
            case 0:
                return globalCache.getComponentStore().numEntities();
            case 1:
                return globalCache.getComponentStore().getComponentCount(componentClasses[0]);
            default:
                return Lists.newArrayList(getEntitiesWith(componentClasses)).size();
        }
    }

    public <T extends Component> Iterable<Map.Entry<EntityRef, T>> listComponents(Class<T> componentClass) {
        TLongObjectIterator<T> iterator = globalCache.getComponentStore().componentIterator(componentClass);
        if (iterator != null) {
            List<Map.Entry<EntityRef, T>> list = new ArrayList<>();
            while (iterator.hasNext()) {
                iterator.advance();
                list.add(new EntityEntry<>(createEntityRef(iterator.key()), iterator.value()));
            }
            return list;
        }
        return Collections.emptyList();
    }

    private static class EntityEntry<T> implements Map.Entry<EntityRef, T> {
        private EntityRef key;
        private T value;

        EntityEntry(EntityRef ref, T value) {
            this.key = ref;
            this.value = value;
        }

        @Override
        public EntityRef getKey() {
            return key;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public T setValue(T newValue) {
            throw new UnsupportedOperationException();
        }
    }

    private class EntityIterable implements Iterable<EntityRef> {
        private TLongList list;

        EntityIterable(TLongList list) {
            this.list = list;
        }

        @Override
        public Iterator<EntityRef> iterator() {
            return new EntityIterator(list.iterator());
        }
    }

    private class EntityIterator implements Iterator<EntityRef> {
        private TLongIterator idIterator;

        EntityIterator(TLongIterator idIterator) {
            this.idIterator = idIterator;
        }

        @Override
        public boolean hasNext() {
            return idIterator.hasNext();
        }

        @Override
        public EntityRef next() {
            return createEntityRef(idIterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public EntityBuilder newBuilder() {
        return globalCache.newBuilder();
    }

    public EntityBuilder newBuilder(String prefabName) {
        return globalCache.newBuilder(prefabName);
    }

    public EntityBuilder newBuilder(Prefab prefab) {
        return globalCache.newBuilder(prefab);
    }

    public EntityRef create() {
        return globalCache.create();
    }

    public EntityRef create(Component... components) {
        return globalCache.create(components);
    }

    public EntityRef create(Iterable<Component> components) {
        return globalCache.create(components);
    }

    public EntityRef create(String prefabName) {
        return globalCache.create(prefabName);
    }

    public EntityRef create(Prefab prefab) {
        return globalCache.create(prefab);
    }

    public EntityRef create(String prefab, Vector3f position) {
        return globalCache.create(prefab, position);
    }

    public EntityRef create(Prefab prefab, Vector3f position) {
        return globalCache.create(prefab, position);
    }

    public EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation) {
        return globalCache.create(prefab, position, rotation);
    }

}
