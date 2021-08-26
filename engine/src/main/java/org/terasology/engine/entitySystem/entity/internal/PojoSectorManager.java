// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import com.google.common.collect.Iterables;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityPool;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PojoSectorManager implements EngineSectorManager {

    private List<EngineEntityPool> pools;

    private PojoEntityManager entityManager;

    public PojoSectorManager(PojoEntityManager entityManager) {
        this.entityManager = entityManager;
        pools = new ArrayList<>();
        pools.add(new PojoEntityPool(entityManager));
    }

    @Override
    public void clear() {
        for (EntityPool pool : pools) {
            pool.clear();
        }
    }

    @Override
    public EntityBuilder newBuilder() {
        return new EntityBuilder(entityManager, this);
    }

    @Override
    public EntityBuilder newBuilder(String prefabName) {
        EntityBuilder builder = newBuilder();
        if (!builder.addPrefab(prefabName)) {
            return null;
        }
        return builder;
    }

    @Override
    public EntityBuilder newBuilder(Prefab prefab) {
        EntityBuilder builder = newBuilder();
        builder.addPrefab(prefab);
        return builder;
    }

    @Override
    public EntityRef create() {
        return getPool().create();
    }

    @Override
    public EntityRef create(Component... components) {
        return getPool().create(components);
    }

    @Override
    public EntityRef create(Iterable<Component> components) {
        return getPool().create(components);
    }

    @Override
    public EntityRef create(Iterable<Component> components, boolean sendLifecycleEvents) {
        return getPool().create(components, sendLifecycleEvents);
    }

    @Override
    public EntityRef create(String prefabName) {
        return getPool().create(prefabName);
    }

    @Override
    public EntityRef create(Prefab prefab) {
        return getPool().create(prefab);
    }

    @Override
    public EntityRef create(String prefab, Vector3fc position) {
        return getPool().create(prefab, position);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3fc position) {
        return getPool().create(prefab, position);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3fc position, Quaternionfc rotation) {
        return getPool().create(prefab, position, rotation);
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components) {
        return getPool().createEntityWithoutLifecycleEvents(components);
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(String prefab) {
        return getPool().createEntityWithoutLifecycleEvents(prefab);
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Prefab prefab) {
        return getPool().createEntityWithoutLifecycleEvents(prefab);
    }

    @Override
    public void putEntity(long entityId, BaseEntityRef ref) {
        getPool().putEntity(entityId, ref);
    }

    @Override
    public ComponentTable getComponentStore() {
        return getPool().getComponentStore();
    }

    @Override
    public EntityRef createEntityWithId(long id, Iterable<Component> components) {
        return getPool().createEntityWithId(id, components);
    }

    public void destroy(long entityId) {
        getPool().destroy(entityId);
    }

    public void destroyEntityWithoutEvents(EntityRef entity) {
        getPool().destroyEntityWithoutEvents(entity);
    }

    @Override
    public Iterable<EntityRef> getAllEntities() {
        List<Iterable<EntityRef>> entityIterables = new ArrayList<>();
        for (EntityPool pool : pools) {
            entityIterables.add(pool.getAllEntities());
        }

        return Iterables.concat(entityIterables);
    }

    @Override
    public Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses) {
        List<Iterable<EntityRef>> entityIterables = new ArrayList<>();
        for (EntityPool pool : pools) {
            entityIterables.add(pool.getEntitiesWith(componentClasses));
        }

        return Iterables.concat(entityIterables);
    }

    @Override
    public int getCountOfEntitiesWith(Class<? extends Component>[] componentClasses) {
        int i = 0;
        for (EngineEntityPool pool : pools) {
            i += pool.getCountOfEntitiesWith(componentClasses);
        }
        return i;
    }

    @Override
    public int getActiveEntityCount() {
        int count = 0;
        for (EntityPool pool : pools) {
            count += pool.getActiveEntityCount();
        }
        return count;
    }

    @Override
    public EntityRef getEntity(long id) {
        return entityManager.getEntity(id);
    }

    private EngineEntityPool getPool() {
        return pools.get(0);
    }

    public boolean hasComponent(long entityId, Class<? extends Component> componentClass) {
        for (EngineEntityPool pool : pools) {
            if (pool.hasComponent(entityId, componentClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<BaseEntityRef> remove(long id) {
        if (contains(id)) {
            return entityManager.getPool(id)
                    .flatMap(pool -> pool.remove(id));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void insertRef(BaseEntityRef ref, Iterable<Component> components) {
        getPool().insertRef(ref, components);
    }

    @Override
    public boolean contains(long id) {
        return pools.stream().anyMatch(pool -> pool.contains(id));
    }

}
