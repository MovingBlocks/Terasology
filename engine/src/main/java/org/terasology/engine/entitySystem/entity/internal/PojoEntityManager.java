// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.sectors.SectorSimulationComponent;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.terasology.engine.entitySystem.entity.internal.EntityScope.SECTOR;

public class PojoEntityManager implements EngineEntityManager {
    public static final long NULL_ID = 0;

    private static final Logger logger = LoggerFactory.getLogger(PojoEntityManager.class);

    private long nextEntityId = 1;
    private TLongSet loadedIds = new TLongHashSet();

    private EngineEntityPool globalPool = new PojoEntityPool(this);
    private PojoSectorManager sectorManager = new PojoSectorManager(this);
    private Map<Long, EngineEntityPool> poolMap = new MapMaker().initialCapacity(1000).makeMap();
    private List<EngineEntityPool> worldPools = Lists.newArrayList();
    private Map<EngineEntityPool, Long> poolCounts = new HashMap<EngineEntityPool, Long>();

    private Set<EntityChangeSubscriber> subscribers = Sets.newLinkedHashSet();
    private Set<EntityDestroySubscriber> destroySubscribers = Sets.newLinkedHashSet();
    private EventSystem eventSystem;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;
    private WorldManager worldManager;
    private GameManifest gameManifest;

    private RefStrategy refStrategy = new DefaultRefStrategy();

    private TypeHandlerLibrary typeSerializerLibrary;

    @Override
    public RefStrategy getEntityRefStrategy() {
        return refStrategy;
    }

    @Override
    public void setEntityRefStrategy(RefStrategy strategy) {
        this.refStrategy = strategy;
    }

    @Override
    public EngineEntityPool getGlobalPool() {
        return globalPool;
    }

    /**
     * Check if world pools have been created and returns them such that subsequent entities are put in them. The global pool
     * is returned if no world pools have been created.
     *
     * @return the pool under consideration.
     */
    public EngineEntityPool getCurrentWorldPool() {
        if (worldManager == null || worldManager.getCurrentWorldPool() == null) {
            return globalPool;
        } else {
            return worldManager.getCurrentWorldPool();
        }
    }

    /**
     * Not all entities are present in the world pools. The world pools are created only
     * in the {@link org.terasology.engine.core.modes.loadProcesses.CreateWorldEntity} process, but much before
     * that some blocks are loaded. Hence those are by default put into the global pool.
     *
     * @return if world pools have been formed or not
     */
    private boolean isWorldPoolGlobalPool() {
        return getCurrentWorldPool() == globalPool;
    }

    @Override
    public void clear() {
        globalPool.clear();
        sectorManager.clear();
        nextEntityId = 1;
        loadedIds.clear();
    }

    @Override
    public EntityBuilder newBuilder() {
        return getCurrentWorldPool().newBuilder();
    }

    @Override
    public EntityBuilder newBuilder(String prefabName) {
        return getCurrentWorldPool().newBuilder(prefabName);
    }

    @Override
    public EntityBuilder newBuilder(Prefab prefab) {
        return getCurrentWorldPool().newBuilder(prefab);
    }

    @Override
    public EntityRef create() {
        return getCurrentWorldPool().create();
    }

    @Override
    public void createWorldPools(GameManifest game) {
        this.gameManifest = game;
        Map<String, WorldInfo> worldInfoMap = gameManifest.getWorldInfoMap();
        worldManager = new WorldManager(gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD));
        for (Map.Entry<String, WorldInfo> worldInfoEntry : worldInfoMap.entrySet()) {
            EngineEntityPool pool = new PojoEntityPool(this);
            //pool.create();
            worldPools.add(pool);
            worldManager.addWorldPool(worldInfoEntry.getValue(), pool);
        }
    }

    @Override
    public EntityRef createSectorEntity(long maxDelta) {
        return createSectorEntity(maxDelta, maxDelta);
    }

    @Override
    public EntityRef createSectorEntity(long unloadedMaxDelta, long loadedMaxDelta) {
        EntityRef entity = sectorManager.create();
        entity.setScope(SECTOR);

        SectorSimulationComponent simulationComponent = entity.getComponent(SectorSimulationComponent.class);
        simulationComponent.unloadedMaxDelta = unloadedMaxDelta;
        simulationComponent.loadedMaxDelta = loadedMaxDelta;
        entity.saveComponent(simulationComponent);

        return entity;
    }

    @Override
    public long createEntity() {
        if (nextEntityId == NULL_ID) {
            nextEntityId++;
        }
        loadedIds.add(nextEntityId);
        return nextEntityId++;
    }

    @Override
    public EntityRef create(Component... components) {
        return getCurrentWorldPool().create(components);
    }

    @Override
    public EntityRef create(Iterable<Component> components) {
        return getCurrentWorldPool().create(components);
    }

    @Override
    public EntityRef create(Iterable<Component> components, boolean sendLifecycleEvents) {
        return getCurrentWorldPool().create(components, sendLifecycleEvents);
    }

    @Override
    public EntityRef create(String prefabName) {
        return getCurrentWorldPool().create(prefabName);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3fc position, Quaternionfc rotation) {
        return getCurrentWorldPool().create(prefab, position, rotation);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3fc position) {
        return getCurrentWorldPool().create(prefab, position);
    }

    @Override
    public EntityRef create(String prefab, Vector3fc position) {
        return getCurrentWorldPool().create(prefab, position);
    }

    @Override
    public EntityRef create(Prefab prefab) {
        return getCurrentWorldPool().create(prefab);
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
        return getCurrentWorldPool().create(newEntityComponents);
    }

    @Override
    public Map<Class<? extends Component>, Component> copyComponents(EntityRef other) {
        Map<Class<? extends Component>, Component> result = Maps.newHashMap();
        for (Component c : other.iterateComponents()) {
            result.put(c.getClass(), componentLibrary.copyWithOwnedEntities(c));
        }
        return result;
    }

    @Override
    public Iterable<EntityRef> getAllEntities() {
        if (isWorldPoolGlobalPool()) {
            return Iterables.concat(globalPool.getAllEntities(), sectorManager.getAllEntities());
        }
        return Iterables.concat(globalPool.getAllEntities(), getCurrentWorldPool().getAllEntities(), sectorManager.getAllEntities());
    }

    @SafeVarargs
    @Override
    public final Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses) {
        if (isWorldPoolGlobalPool()) {
            return Iterables.concat(globalPool.getEntitiesWith(componentClasses),
                    sectorManager.getEntitiesWith(componentClasses));
        }
        return Iterables.concat(globalPool.getEntitiesWith(componentClasses),
                getCurrentWorldPool().getEntitiesWith(componentClasses), sectorManager.getEntitiesWith(componentClasses));
    }

    @Override
    public int getActiveEntityCount() {
        if (isWorldPoolGlobalPool()) {
            return globalPool.getActiveEntityCount() + sectorManager.getActiveEntityCount();
        }
        return globalPool.getActiveEntityCount() + getCurrentWorldPool().getActiveEntityCount()
                + sectorManager.getActiveEntityCount();

    }

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    public void setComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
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

    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }


    /*
     * Engine features
     */

    @Override
    public EntityRef getEntity(long id) {
        return getPool(id)
                .map(pool -> pool.getEntity(id))
                .orElse(EntityRef.NULL);
    }

    @Override
    public List<EngineEntityPool> getWorldPools() {
        return worldPools;
    }

    @Override
    public Map<WorldInfo, EngineEntityPool> getWorldPoolsMap() {
        return worldManager.getWorldPoolMap();
    }

    @Override
    public Map<Long, EngineEntityPool> getPoolMap() {
        return poolMap;
    }

    @Override
    public Map<EngineEntityPool, Long> getPoolCounts() {
        return poolCounts;
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components) {
        return getCurrentWorldPool().createEntityWithoutLifecycleEvents(components);
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(String prefabName) {
        return getCurrentWorldPool().createEntityWithoutLifecycleEvents(prefabName);
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Prefab prefab) {
        return getCurrentWorldPool().createEntityWithoutLifecycleEvents(prefab);
    }

    @Override
    public void putEntity(long entityId, BaseEntityRef ref) {
        getCurrentWorldPool().putEntity(entityId, ref);
    }

    @Override
    public ComponentTable getComponentStore() {
        return getCurrentWorldPool().getComponentStore();
    }

    @Override
    public void destroyEntityWithoutEvents(EntityRef entity) {
        getCurrentWorldPool().destroyEntityWithoutEvents(entity);
    }

    @Override
    public EntityRef createEntityWithId(long id, Iterable<Component> components) {
        EntityBuilder builder = newBuilder();
        builder.setId(id);
        builder.addComponents(components);
        return builder.build();
    }

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
    public TypeHandlerLibrary getTypeSerializerLibrary() {
        return typeSerializerLibrary;
    }

    public void setTypeSerializerLibrary(TypeHandlerLibrary serializerLibrary) {
        this.typeSerializerLibrary = serializerLibrary;
    }

    @Override
    public EngineSectorManager getSectorManager() {
        return sectorManager;
    }

    @Override
    public void deactivateForStorage(EntityRef entity) {
        if (!entity.exists()) {
            return;
        }

        long entityId = entity.getId();
        if (eventSystem != null) {
            eventSystem.send(entity, BeforeDeactivateComponent.newInstance());
        }

        List<Component> components = Collections.unmodifiableList(
                getPool(entityId)
                        .map(pool -> pool.getComponentStore().getComponentsInNewList(entityId))
                        .orElse(Collections.emptyList()));

        notifyBeforeDeactivation(entity, components);
        for (Component component : components) {
            getPool(entityId).ifPresent(pool -> pool.getComponentStore().remove(entityId, component.getClass()));
        }
        loadedIds.remove(entityId);
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
        return globalPool.getComponentStore().get(entityId, componentClass) != null
                || getCurrentWorldPool().getComponentStore().get(entityId, componentClass) != null
                || sectorManager.hasComponent(entityId, componentClass);
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
    public Iterable<Component> iterateComponents(long entityId) {
        return getPool(entityId)
                .map(pool -> pool.getComponentStore().iterateComponents(entityId))
                .orElse(Collections.emptyList());
    }

    @Override
    public void destroy(long entityId) {
        getPool(entityId).ifPresent(pool -> pool.destroy(entityId));
    }

    protected void notifyComponentRemovalAndEntityDestruction(long entityId, EntityRef ref) {
        getPool(entityId)
                .map(pool -> pool.getComponentStore().iterateComponents(entityId))
                .orElse(Collections.emptyList())
                .forEach(comp -> notifyComponentRemoved(ref, comp.getClass()));

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
    public <T extends Component> T getComponent(long entityId, Class<T> componentClass) {
        return getPool(entityId)
                .map(pool -> pool.getComponentStore().get(entityId, componentClass))
                .orElse(null);
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
    public <T extends Component> T addComponent(long entityId, T component) {
        Preconditions.checkNotNull(component);
        Optional<Component> oldComponent = getPool(entityId).map(pool -> pool.getComponentStore().put(entityId, component));

        // notify internal users first to get the unobstructed views on the entity as it is at this moment.
        if (!oldComponent.isPresent()) {
            notifyComponentAdded(getEntity(entityId), component.getClass());
        } else {
            logger.error("Adding a component ({}) over an existing component for entity {}", component.getClass(), entityId); //NOPMD
            notifyComponentChanged(getEntity(entityId), component.getClass());
        }

        // Send life cycle events for arbitrary systems to react on.
        // Note: systems are free to remove the component that was just added, which might cause some trouble here...
        if (eventSystem != null) {
            EntityRef entityRef = getEntity(entityId);
            if (!oldComponent.isPresent()) {
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
    public <T extends Component> T removeComponent(long entityId, Class<T> componentClass) {
        Optional<ComponentTable> maybeStore = getPool(entityId).map(EngineEntityPool::getComponentStore);
        Optional<T> component = maybeStore.map(store -> store.get(entityId, componentClass));

        if (component.isPresent()) {
            if (eventSystem != null) {
                EntityRef entityRef = getEntity(entityId);
                eventSystem.send(entityRef, BeforeDeactivateComponent.newInstance(), component.get());
                eventSystem.send(entityRef, BeforeRemoveComponent.newInstance(), component.get());
            }
            notifyComponentRemoved(getEntity(entityId), componentClass);
            maybeStore.ifPresent(store -> store.remove(entityId, componentClass));
        }
        return component.orElse(null);
    }

    /**
     * Saves a component to an entity
     *
     * @param entityId
     * @param component
     */
    @Override
    public void saveComponent(long entityId, Component component) {
        Optional<Component> oldComponent = getPool(entityId)
                .map(pool -> pool.getComponentStore().put(entityId, component));

        if (!oldComponent.isPresent()) {
            logger.error("Saving a component ({}) that doesn't belong to this entity {}", component.getClass(), entityId); //NOPMD
        }
        if (eventSystem != null) {
            EntityRef entityRef = getEntity(entityId);
            if (!oldComponent.isPresent()) {
                eventSystem.send(entityRef, OnAddedComponent.newInstance(), component);
                eventSystem.send(entityRef, OnActivatedComponent.newInstance(), component);
            } else {
                eventSystem.send(entityRef, OnChangedComponent.newInstance(), component);
            }
        }
        if (!oldComponent.isPresent()) {
            notifyComponentAdded(getEntity(entityId), component.getClass());
        } else {
            notifyComponentChanged(getEntity(entityId), component.getClass());
        }
    }


    /*
     * Implementation
     */

    public Optional<EngineEntityPool> getPool(long id) {
        Optional<EngineEntityPool> pool = Optional.ofNullable(poolMap.get(id));
        // TODO: Entity pools assignment is not needed as of now, can be enabled later on when necessary.
        if (!pool.isPresent() && id != NULL_ID && !isExistingEntity(id)) {
            logger.error("Entity {} doesn't exist", id);
        }
        return pool;
    }

    /**
     * Assign the given entity to the given pool.
     * <p>
     * If the entity is already assigned to a pool, it will be re-assigned to the given pool.
     * This does not actually move the entity or any of its components.
     * If you want to move an entity to a different pool, {@link #moveToPool(long, EngineEntityPool)} should be used
     * instead.
     *
     * @param entityId the id of the entity to assign
     * @param pool     the pool to assign the entity to
     */
    @Override
    public void assignToPool(long entityId, EngineEntityPool pool) {
        if (poolMap.get(entityId) != pool) {
            poolMap.put(entityId, pool);
            if (!poolCounts.containsKey(pool)) {
                poolCounts.put(pool, 1L);
            } else {
                poolCounts.put(pool, poolCounts.get(pool) + 1L);
            }
        }
    }

    /**
     * Remove the assignment of an entity to a pool.
     * <p>
     * This does not affect anything else related to the entity, but may lead to the entity or its components being
     * unable to be found.
     * <p>
     * When using this method, be sure to properly re-assign the entity to its correct pool afterwards.
     *
     * @param id the id of the entity to remove the assignment for
     */
    protected void unassignPool(long id) {
        poolMap.remove(id);
    }

    @Override
    public boolean moveToPool(long id, EngineEntityPool pool) {

        if (getPool(id).isPresent() && getPool(id).get().equals(pool)) {
            //The entity is already in the correct pool
            return true;
        }

        //Save the current entity and components
        Optional<EngineEntityPool> maybePool = getPool(id);
        EngineEntityPool oldPool;
        if (!maybePool.isPresent()) {
            return false;
        } else {
            oldPool = maybePool.get();
        }
        Map<Class<? extends Component>, Component> savedComponents = copyComponents(oldPool.getEntity(id));

        //Remove from the existing pool
        Optional<BaseEntityRef> maybeRef = oldPool.remove(id);
        //Decrease the count of entities in that pool
        poolCounts.put(oldPool, poolCounts.get(oldPool) - 1);
        if (!maybeRef.isPresent()) {
            return false;
        }
        BaseEntityRef ref = maybeRef.get();

        //Create in new pool
        pool.insertRef(ref, savedComponents.values());

        //TODO: send events?

        return true;
    }

    @Override
    public void notifyComponentAdded(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentAdded(changedEntity, component);
        }
    }

    protected void notifyComponentRemoved(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentRemoved(changedEntity, component);
        }
    }

    protected void notifyComponentChanged(EntityRef changedEntity, Class<? extends Component> component) {
        for (EntityChangeSubscriber subscriber : subscribers) {
            subscriber.onEntityComponentChange(changedEntity, component);
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

    @Override
    @SafeVarargs
    public final int getCountOfEntitiesWith(Class<? extends Component>... componentClasses) {
        if (isWorldPoolGlobalPool()) {
            return globalPool.getCountOfEntitiesWith(componentClasses) +
                    sectorManager.getCountOfEntitiesWith(componentClasses);
        }
        return sectorManager.getCountOfEntitiesWith(componentClasses) +
                getCurrentWorldPool().getCountOfEntitiesWith(componentClasses) +
                globalPool.getCountOfEntitiesWith(componentClasses);
    }

    public <T extends Component> Iterable<Map.Entry<EntityRef, T>> listComponents(Class<T> componentClass) {
        List<TLongObjectIterator<T>> iterators = new ArrayList<>();
        if (isWorldPoolGlobalPool()) {
            iterators.add(globalPool.getComponentStore().componentIterator(componentClass));
        } else {
            iterators.add(globalPool.getComponentStore().componentIterator(componentClass));
            iterators.add(getCurrentWorldPool().getComponentStore().componentIterator(componentClass));
        }
        iterators.add(sectorManager.getComponentStore().componentIterator(componentClass));

        List<Map.Entry<EntityRef, T>> list = new ArrayList<>();
        for (TLongObjectIterator<T> iterator : iterators) {
            if (iterator != null) {
                while (iterator.hasNext()) {
                    iterator.advance();
                    list.add(new EntityEntry<>(getEntity(iterator.key()), iterator.value()));
                }
            }
        }
        return list;
    }

    @Override
    public boolean registerId(long entityId) {
        if (entityId >= nextEntityId) {
            logger.error("Prevented attempt to create entity with an invalid id.");
            return false;
        }
        loadedIds.add(entityId);
        return true;
    }

    protected boolean idLoaded(long entityId) {
        return loadedIds.contains(entityId);
    }

    @Override
    public Optional<BaseEntityRef> remove(long id) {
        return getPool(id)
                .flatMap(pool -> pool.remove(id));
    }

    @Override
    public void insertRef(BaseEntityRef ref, Iterable<Component> components) {
        globalPool.insertRef(ref, components);
    }

    @Override
    public boolean contains(long id) {
        return globalPool.contains(id) || sectorManager.contains(id);
    }

    /**
     * Remove this id from the entity manager's list of loaded ids.
     *
     * @param id the id to remove
     */
    protected void unregister(long id) {
        loadedIds.remove(id);
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

}
