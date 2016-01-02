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
package org.terasology.persistence.internal;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.persistence.serializers.FieldSerializeCheck;
import org.terasology.protobuf.EntityData;

import java.util.Map;
import java.util.Set;

/**
 * Utility class for the construction of a EntityData.EntityStore structure for storing the entities on disk..
 *
 */
final class EntityStorer {

    private final EntitySerializer serializer;
    private final EntityData.EntityStore.Builder entityStoreBuilder;
    private final OwnershipHelper helper;
    private Set<EntityRef> storedEntities = Sets.newHashSet();

    public EntityStorer(EngineEntityManager entityManager) {
        this.entityStoreBuilder = EntityData.EntityStore.newBuilder();
        this.serializer = new EntitySerializer(entityManager);
        this.helper = new OwnershipHelper(entityManager.getComponentLibrary());

        Map<Class<? extends Component>, Integer> componentIds = Maps.newHashMap();

        for (ComponentMetadata<?> componentMetadata : entityManager.getComponentLibrary().iterateComponentMetadata()) {
            entityStoreBuilder.addComponentClass(componentMetadata.getUri().toString());
            componentIds.put(componentMetadata.getType(), componentIds.size());
        }
        serializer.setComponentIdMapping(componentIds);
    }
    public void store(EntityRef entity) {
        store(entity, "");
    }

    public void store(EntityRef entity, String name) {
        if (entity.isActive()) {
            for (EntityRef ownedEntity : helper.listOwnedEntities(entity)) {
                if (!ownedEntity.isAlwaysRelevant() && ownedEntity.isPersistent()) {
                    store(ownedEntity);
                }
            }
            EntityData.Entity entityData = serializer.serialize(entity, true, FieldSerializeCheck.NullCheck.<Component>newInstance());
            entityStoreBuilder.addEntity(entityData);
            if (!name.isEmpty()) {
                entityStoreBuilder.addEntityName(name);
                entityStoreBuilder.addEntityNamed(entityData.getId());
            }
            storedEntities.add(entity);
        }
    }

    public EntityData.EntityStore finaliseStore() {
        return entityStoreBuilder.build();
    }

    /**
     *
     * @return all entities stored directly or indirectly (owned entities) via the store methods.
     */
    public Set<EntityRef> getStoredEntities() {
        return storedEntities;
    }
}
