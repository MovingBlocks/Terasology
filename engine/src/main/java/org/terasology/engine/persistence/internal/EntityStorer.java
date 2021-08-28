// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.persistence.serializers.EntitySerializer;
import org.terasology.engine.persistence.serializers.FieldSerializeCheck;
import org.terasology.engine.persistence.serializers.PersistenceComponentSerializeCheck;
import org.terasology.gestalt.entitysystem.component.Component;
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

    EntityStorer(EngineEntityManager entityManager) {
        this.entityStoreBuilder = EntityData.EntityStore.newBuilder();
        this.serializer = new EntitySerializer(entityManager);
        this.serializer.setComponentSerializeCheck(new PersistenceComponentSerializeCheck());
        this.helper = new OwnershipHelper(entityManager.getComponentLibrary());

        Map<Class<? extends Component>, Integer> componentIds = Maps.newHashMap();

        for (ComponentMetadata<?> componentMetadata : entityManager.getComponentLibrary().iterateComponentMetadata()) {
            entityStoreBuilder.addComponentClass(componentMetadata.getId().toString());
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
