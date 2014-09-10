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
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.persistence.serializers.FieldSerializeCheck;
import org.terasology.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * @author Immortius
 */
final class EntityStorer implements EntityRefTypeHandler.EntityRefInterceptor {

    private final EngineEntityManager entityManager;
    private final EntitySerializer serializer;
    private final EntityData.EntityStore.Builder entityStoreBuilder;
    private final OwnershipHelper helper;
    private TIntSet externalReferences = new TIntHashSet();
    private TIntSet storedEntityIds = new TIntHashSet();

    public EntityStorer(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
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

    public void store(EntityRef entity, boolean deactivate) {
        store(entity, "", deactivate);
    }

    public void store(EntityRef entity, String name, boolean deactivate) {
        if (entity.isActive()) {
            for (EntityRef ownedEntity : helper.listOwnedEntities(entity)) {
                if (!ownedEntity.isAlwaysRelevant()) {
                    if (!ownedEntity.isPersistent()) {
                        if (deactivate) {
                            ownedEntity.destroy();
                        }
                    } else {
                        store(ownedEntity, deactivate);
                    }
                }
            }
            EntityRefTypeHandler.setReferenceInterceptor(this);
            EntityData.Entity entityData = serializer.serialize(entity, true, FieldSerializeCheck.NullCheck.<Component>newInstance());
            EntityRefTypeHandler.setReferenceInterceptor(null);
            entityStoreBuilder.addEntity(entityData);
            if (!name.isEmpty()) {
                entityStoreBuilder.addEntityName(name);
                entityStoreBuilder.addEntityNamed(entityData.getId());
            }
            storedEntityIds.add(entityData.getId());
            externalReferences.remove(entityData.getId());
            if (deactivate) {
                entityManager.deactivateForStorage(entity);
            }
        }
    }

    public EntityData.EntityStore finaliseStore() {
        return entityStoreBuilder.build();
    }

    public TIntSet getExternalReferences() {
        return externalReferences;
    }

    @Override
    public boolean loadingRef(int id) {
        return true;
    }

    @Override
    public boolean savingRef(EntityRef ref) {
        if (!ref.isPersistent()) {
            return false;
        }
        if (!storedEntityIds.contains(ref.getId())) {
            externalReferences.add(ref.getId());
        }
        return true;
    }
}
