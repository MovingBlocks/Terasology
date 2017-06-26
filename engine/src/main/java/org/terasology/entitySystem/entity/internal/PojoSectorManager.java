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

import com.google.common.collect.Iterables;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityCache;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.SectorManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class PojoSectorManager implements EngineSectorManager {

    private List<EngineEntityCache> caches;

    private PojoEntityManager entityManager;

    public PojoSectorManager(PojoEntityManager entityManager) {
        this.entityManager = entityManager;
        caches = new ArrayList<>();
        caches.add(new PojoEntityCache(entityManager));
    }

    @Override
    public void clear() {
        for (EntityCache cache : caches) {
            cache.clear();
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
        return getCache().create();
    }

    @Override
    public EntityRef create(Component... components) {
        return getCache().create(components);
    }

    @Override
    public EntityRef create(Iterable<Component> components) {
        return getCache().create(components);
    }

    @Override
    public EntityRef create(Iterable<Component> components, boolean sendLifecycleEvents) {
        return getCache().create(components, sendLifecycleEvents);
    }

    @Override
    public EntityRef create(String prefabName) {
        return getCache().create(prefabName);
    }

    @Override
    public EntityRef create(Prefab prefab) {
        return getCache().create(prefab);
    }

    @Override
    public EntityRef create(String prefab, Vector3f position) {
        return getCache().create(prefab, position);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position) {
        return getCache().create(prefab, position);
    }

    @Override
    public EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation) {
        return getCache().create(prefab, position, rotation);
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components) {
        return getCache().createEntityWithoutLifecycleEvents(components);
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(String prefab) {
        return getCache().createEntityWithoutLifecycleEvents(prefab);
    }

    @Override
    public EntityRef createEntityWithoutLifecycleEvents(Prefab prefab) {
        return getCache().createEntityWithoutLifecycleEvents(prefab);
    }

    @Override
    public void putEntity(long entityId, BaseEntityRef ref) {
        getCache().putEntity(entityId, ref);
    }

    @Override
    public ComponentTable getComponentStore() {
        return getCache().getComponentStore();
    }

    @Override
    public EntityRef createEntityWithId(long id, Iterable<Component> components) {
        return getCache().createEntityWithId(id, components);
    }

    @Override
    public EntityRef createEntityRefWithId(long id) {
        return getCache().createEntityRefWithId(id);
    }

    public void destroy(long entityId) {
        getCache().destroy(entityId);
    }

    public void destroyEntityWithoutEvents(EntityRef entity) {
        getCache().destroyEntityWithoutEvents(entity);
    }

    @Override
    public Iterable<EntityRef> getAllEntities() {
        List<Iterable<EntityRef>> entityIterables = new ArrayList<>();
        for (EntityCache cache : caches) {
            entityIterables.add(cache.getAllEntities());
        }

        return Iterables.concat(entityIterables);
    }

    @Override
    public Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses) {
        return getCache().getEntitiesWith(componentClasses);
    }

    @Override
    public int getCountOfEntitiesWith(Class<? extends Component>[] componentClasses) {
        int i = 0;
        for (EngineEntityCache cache : caches) {
            i += cache.getCountOfEntitiesWith(componentClasses);
        }
        return i;
    }

    @Override
    public int getActiveEntityCount() {
        int count = 0;
        for (EntityCache cache : caches) {
            count += cache.getActiveEntityCount();
        }
        return count;
    }

    @Override
    public EntityRef getExistingEntity(long id) {
        EntityRef entity;
        for (EntityCache cache : caches) {
            entity = cache.getExistingEntity(id);
            if (entity != EntityRef.NULL && entity != null) {
                return entity;
            }
        }
        return EntityRef.NULL;
    }

    private EngineEntityCache getCache() {
        return caches.get(0);
    }

    public boolean hasComponent(long entityId, Class<? extends Component> componentClass) {
        for (EngineEntityCache cache : caches) {
            if (cache.hasComponent(entityId, componentClass)) {
                return true;
            }
        }
        return false;
    }

}
