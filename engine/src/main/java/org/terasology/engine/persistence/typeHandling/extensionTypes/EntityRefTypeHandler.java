// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import gnu.trove.iterator.TLongIterator;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.List;
import java.util.Optional;

public class EntityRefTypeHandler extends TypeHandler<EntityRef> {

    private EngineEntityManager entityManager;

    public EntityRefTypeHandler(EngineEntityManager engineEntityManager) {
        this.entityManager = engineEntityManager;
    }

    @Override
    public PersistedData serializeNonNull(EntityRef value, PersistedDataSerializer serializer) {
        if (value.exists() && value.isPersistent()) {
            return serializer.serialize(value.getId());
        }
        return serializer.serializeNull();
    }

    @Override
    public Optional<EntityRef> deserialize(PersistedData data) {
        if (data.isNumber()) {
            return Optional.ofNullable(entityManager.getEntity(data.getAsLong()));
        }
        return Optional.ofNullable(EntityRef.NULL);
    }

    private void addEntitiesFromLongArray(List<EntityRef> result, PersistedDataArray array) {
        TLongIterator iterator = array.getAsLongArray().iterator();
        while (iterator.hasNext()) {
            long item = iterator.next();
            result.add(entityManager.getEntity(item));
        }
    }

}
