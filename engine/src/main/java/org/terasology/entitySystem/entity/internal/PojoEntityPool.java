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
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.terasology.entitySystem.entity.internal.PojoEntityManager.NULL_ID;

public class PojoEntityPool implements EngineEntityPool {

    private PojoEntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(PojoEntityPool.class);

    private Map<Long, BaseEntityRef> entityStore = new MapMaker().weakValues().concurrencyLevel(4).initialCapacity(1000).makeMap();
    private ComponentTable componentStore = new ComponentTable();

    public PojoEntityPool(PojoEntityManager entityManager) {
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
        return create((Prefab) null, (Vector3f) null, null);
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
        EntityBuilder builder = newBuilder();
        builder.addComponents(components);
        builder.setSendLifecycleEvents(sendLifecycleEvents);
        return builder.build();
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
    public EntityRef create(String prefabName, Vector3fc position) {
        return create(prefabName, JomlUtil.from(position), null);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position) {
        return create(prefab, position, null);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3fc position) {
        return create(prefab, JomlUtil.from(position), null);
    }

    @Override
    public EntityRef create(Prefab prefab) {
        return create(prefab, (Vector3f) null, null);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation) {
        return create(prefab, position, rotation, true);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3fc position, Quaternionfc rotation) {
        return create(prefab, JomlUtil.from(position), JomlUtil.from(rotation), true);
    }

    private EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation, boolean sendLifecycleEvents) {
        EntityBuilder builder = newBuilder(prefab);
        builder.setSendLifecycleEvents(sendLifecycleEvents);

        LocationComponent loc = builder.getComponent(LocationComponent.class);
        if (loc == null && (position != null || rotation != null)) {
            loc = new LocationComponent();
            builder.addComponent(loc);
        }

        if (position != null) {
            loc.setWorldPosition(position);
        }
        if (rotation != null) {
            loc.setWorldRotation(rotation);
        }

        return builder.build();
    }

    private EntityRef create(String prefabName, Vector3f position, Quat4f rotation) {
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
     * @param entityId the id of the entity to destroy
     */
    @Override
    public void destroy(long entityId) {
        // Don't allow the destruction of unloaded entities.
        if (!entityManager.idLoaded(entityId)) {
            return;
        }
        EntityRef ref = getEntity(entityId);

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
        entityManager.unregister(entityId);
        ref.invalidate();
        componentStore.remove(entityId);
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
        EntityBuilder builder = newBuilder();
        builder.setId(id);
        builder.addComponents(components);
        return builder.build();
    }

    @Override
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

    @Override
    public EntityRef getEntity(long entityId) {
        if (entityId == NULL_ID || !entityManager.isExistingEntity(entityId)) {
            // ID is null or the entity doesn't exist
            return EntityRef.NULL;
        }

        EntityRef existing = entityStore.get(entityId);
        if (existing != EntityRef.NULL && existing != null) {
            // Entity already has a ref
            return existing;
        }

        // Create a new ref
        BaseEntityRef entity = entityManager.getEntityRefStrategy().createRefFor(entityId, entityManager);

        entityStore.put(entityId, entity);
        entityManager.assignToPool(entityId, this);
        return entity;
    }

    @SafeVarargs
    @Override
    public final Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses) {
        return () -> entityStore.keySet().stream()
                //Keep entities which have all of the required components
                .filter(id -> Arrays.stream(componentClasses)
                        .allMatch(component -> componentStore.get(id, component) != null))
                .map(id -> getEntity(id))
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
    public Iterable<EntityRef> getAllEntities() {
        return () -> new EntityIterator(componentStore.entityIdIterator(), this);
    }

    @Override
    public boolean hasComponent(long entityId, Class<? extends Component> componentClass) {
        return componentStore.get(entityId, componentClass) != null;
    }

    @Override
    public Optional<BaseEntityRef> remove(long id) {
        componentStore.remove(id);
        entityManager.unassignPool(id);
        return Optional.of(entityStore.remove(id));
    }

    @Override
    public void insertRef(BaseEntityRef ref, Iterable<Component> components) {
        entityStore.put(ref.getId(), ref);
        components.forEach(comp -> componentStore.put(ref.getId(), comp));
        entityManager.assignToPool(ref.getId(), this);
    }

    @Override
    public boolean contains(long id) {
        return entityStore.containsKey(id);
    }

}
