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

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeEntityCreated;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.protobuf.EntityData;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.terasology.entitySystem.entity.internal.PojoEntityManager.NULL_ID;

/**
 */
public class PojoEntityCache implements EngineEntityCache {

    private PojoEntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(PojoEntityCache.class);

    private Map<Long, BaseEntityRef> entityStore = new MapMaker().weakValues().concurrencyLevel(4).initialCapacity(1000).makeMap();
    private ComponentTable componentStore = new ComponentTable();

    public PojoEntityCache(PojoEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void clear() {
        //Todo: should also clear out ids from the EntityManager
        for (EntityRef entity : entityStore.values()) {
            entity.invalidate();
        }
        componentStore.clear();
        entityStore.clear();
    }


    @Override
    public EntityRef create() {
        return create((Prefab) null, null, null);
    }

    @Override
    public EntityRef create(Component... components) {
        return create(Arrays.asList(components));
    }

    @Override
    public EntityRef create(Iterable<Component> components) {
        return create(components, true);
    }

    @Override
    public EntityRef create(Iterable<Component> components, boolean sendLifecycleEvents) {
        components = (components == null) ? Collections.EMPTY_LIST : components;
        EntityRef entity = createEntity(components);

        if (sendLifecycleEvents) {
            EventSystem eventSystem = entityManager.getEventSystem();
            if (eventSystem != null) {
                eventSystem.send(entity, OnAddedComponent.newInstance());
                eventSystem.send(entity, OnActivatedComponent.newInstance());
            }
        }

        for (Component component: components) {
            entityManager.notifyComponentAdded(entity, component.getClass());
        }
        return entity;
    }

    @Override
    public EntityRef create(String prefabName) {
        return create(prefabName, null, null);
    }

    @Override
    public EntityRef create(String prefabName, Vector3f position) {
        return create(prefabName, position, null);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position) {
        return create(prefab, position, null);
    }

    @Override
    public EntityRef create(Prefab prefab) {
        return create(prefab, null, null);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation) {
        return create(prefab, position, rotation, true);
    }

    //@Override
    private EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation, boolean sendLifecycleEvents) {
        EntityBuilder builder = newBuilder(prefab);
        builder.setSendLifecycleEvents(sendLifecycleEvents);

        LocationComponent locationComponent = builder.getComponent(LocationComponent.class);
        if (locationComponent != null) {
            if (position != null) {
                locationComponent.setWorldPosition(position);
            }
            if (rotation != null) {
                locationComponent.setWorldRotation(rotation);
            }
        }

        return builder.build();
    }

    //@Override
    public EntityRef create(String prefabName, Vector3f position, Quat4f rotation) {
        return create(prefabName, position, rotation, true);
    }

    private EntityRef create(String prefabName, Vector3f position, Quat4f rotation, boolean sendLifecycleEvents) {
        Prefab prefab;
        if (prefabName == null || prefabName.isEmpty()) {
            prefab = null;
        } else {
            prefab = entityManager.getPrefabManager().getPrefab(prefabName);

            if (prefab == null) {
                logger.warn("Unable to instantiate unknown prefab: \"{}\"", prefabName);
                return EntityRef.NULL;
            }
        }
        return create(prefab, position, rotation, sendLifecycleEvents);
    }

    /**
     * Destroys this entity, sending event
     *
     * @param entityId
     */
    @Override
    public void destroy(long entityId) {
        // Don't allow the destruction of unloaded entities.
        if (!entityManager.idLoaded(entityId)) {
            return;
        }
        EntityRef ref = createEntityRef(entityId);

        EventSystem eventSystem = entityManager.getEventSystem();
        if (eventSystem != null) {
            eventSystem.send(ref, BeforeDeactivateComponent.newInstance());
            eventSystem.send(ref, BeforeRemoveComponent.newInstance());
        }
        entityManager.notifyComponentRemovalAndEntityDestruction(entityId, ref);
        destroy(ref);
    }

    private void destroy(EntityRef ref) {
        // Don't allow the destruction of unloaded entities.
        long entityId = ref.getId();
        entityStore.remove(entityId);
        entityManager.remove(entityId);
        ref.invalidate();
        componentStore.remove(entityId);
    }

    private EntityRef createEntity(Iterable<Component> components) {
        long entityId = entityManager.createEntity(this);

        Prefab prefab = null;
        for (Component component : components) {
            if (component instanceof EntityInfoComponent) {
                EntityInfoComponent comp = (EntityInfoComponent) component;
                prefab = comp.parentPrefab;
                break;
            }
        }

        Iterable<Component> finalComponents;
        EventSystem eventSystem = entityManager.getEventSystem();
        if (eventSystem != null) {
            BeforeEntityCreated event = new BeforeEntityCreated(prefab, components);
            BaseEntityRef tempRef = entityManager.getEntityRefStrategy().createRefFor(entityId, entityManager);
            eventSystem.send(tempRef, event);
            tempRef.invalidate();
            finalComponents = event.getResultComponents();
        } else {
            finalComponents = components;
        }

        for (Component c : finalComponents) {
            componentStore.put(entityId, c);
        }
        return createEntityRef(entityId);
    }

    @Override
    public EntityRef createEntityRefWithId(long id) {
        if (entityManager.isExistingEntity(id)) {
            return createEntityRef(id);
        }
        return EntityRef.NULL;
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components) {
        return create(components, false);
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(String prefabName) {
        return create(prefabName, null, null, false);
    }

    /**
     * Creates the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Prefab prefab) {
        return create(prefab, null, null, false);
    }

    /**
     * Destroys the entity without sending any events. The entity life cycle subscriber will however be informed.
     */
    @Override
    public void destroyEntityWithoutEvents(EntityRef entity) {
        if (entity.isActive()) {
            entityManager.notifyComponentRemovalAndEntityDestruction(entity.getId(), entity);
            destroy(entity);
        }
    }

    @Override
    public EntityRef createEntityWithId(long id, Iterable<Component> components) {
        if (!entityManager.registerId(id)) {
            return EntityRef.NULL;
        }

        components = (components == null) ? Collections.EMPTY_LIST : components;

        for (Component c : components) {
            componentStore.put(id, c);
        }

        EntityRef entity = createEntityRef(id);
        EventSystem eventSystem = entityManager.getEventSystem();
        if (eventSystem != null) {
            eventSystem.send(entity, OnActivatedComponent.newInstance());
        }

        for (Component component : components) {
            entityManager.notifyComponentAdded(entity, component.getClass());
        }
        return entity;
    }

    public EntityBuilder newBuilder() {
        return new EntityBuilder(entityManager, this);
    }

    @Override
    public EntityBuilder newBuilder(String prefabName) {
        EntityBuilder builder = newBuilder();
        if (!builder.addPrefab(prefabName)) {
            logger.warn("Unable to instantiate unknown prefab: \"{}\"", prefabName);
        }
        return builder;
    }

    @Override
    public EntityBuilder newBuilder(Prefab prefab) {
        EntityBuilder builder = newBuilder();
        builder.addPrefab(prefab);
        return builder;
    }

    /**
     * Gets the internal entity store.
     * <p>
     * It is returned as an unmodifiable map, so cannot be edited. Use {@link #putEntity} to modify the map.
     *
     * @return an unmodifiable version of the internal entity store
     */
    protected Map<Long, BaseEntityRef> getEntityStore() {
        return Collections.unmodifiableMap(entityStore);
    }

    /**
     * Puts an entity into the internal storage.
     * <p>
     * This is intended for use by the {@link PojoEntityManager}.
     * In most cases, it is better to use the {@link #create} or {@link #newBuilder} methods instead.
     *
     * @param entityId the id of the entity to add
     * @param ref the {@link BaseEntityRef} to add
     */
    @Override
    public void putEntity(long entityId, BaseEntityRef ref) {
        entityStore.put(entityId, ref);
    }

    @Override
    public ComponentTable getComponentStore() {
        return componentStore;
    }

    private EntityRef createEntityRef(long entityId) {
        if (entityId == NULL_ID) {
            return EntityRef.NULL;
        }
        EntityRef existing = entityManager.getEntity(entityId);
        if (existing != null) {
            //Entity exists, but is not in this cache
            return existing;
        }
        //Todo: look into whether RefStrategy should use manager or cache?
        BaseEntityRef newRef = entityManager.getEntityRefStrategy().createRefFor(entityId, entityManager);

        if (newRef.getComponent(EntityInfoComponent.class).scope == EntityData.Entity.Scope.SECTOR) {
            entityManager.assignToCache(newRef, entityManager.getSectorManager());
        } else {
            entityManager.assignToCache(newRef, entityManager);
        }

        entityStore.put(entityId, newRef);
        return newRef;
    }

    @SafeVarargs
    @Override
    public final Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses) {
        return () -> entityStore.keySet().stream()
                //Keep entities which have all of the required components
                .filter(id -> Arrays.stream(componentClasses)
                        .allMatch(component -> componentStore.get(id, component) != null))
                .map(id -> createEntityRef(id))
                .iterator();
    }

    @Override
    public int getCountOfEntitiesWith(Class<? extends Component>[] componentClasses) {
        switch (componentClasses.length) {
            case 0:
                return componentStore.numEntities();
            case 1:
                return componentStore.getComponentCount(componentClasses[0]);
            default:
                return Lists.newArrayList(getEntitiesWith(componentClasses)).size();
        }
    }

    @Override
    public int getActiveEntityCount() {
        return entityStore.size();
    }

    @Override
    public EntityRef getExistingEntity(long id) {
        EntityRef entity = entityStore.get(id);
        return (entity == null) ? EntityRef.NULL : entity;
    }

    @Override
    public Iterable<EntityRef> getAllEntities() {
        return () -> new EntityIterator(componentStore.entityIdIterator(), this);
    }

    @Override
    public boolean hasComponent(long entityId, Class<? extends Component> componentClass) {
        return componentStore.get(entityId, componentClass) != null;
    }

}
