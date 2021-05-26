// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializerImpl;
import org.terasology.protobuf.EntityData;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("TteTest")
public class WorldSerializerTest extends TerasologyTestingEnvironment {

    @Test
    public void testNotPersistedIfFlagedOtherwise() throws Exception {
        EngineEntityManager entityManager = context.get(EngineEntityManager.class);
        EntityBuilder entityBuilder = entityManager.newBuilder();
        PrefabSerializer prefabSerializer = new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary());
        WorldSerializer worldSerializer = new WorldSerializerImpl(entityManager, prefabSerializer);
        entityBuilder.setPersistent(false);
        @SuppressWarnings("unused") // just used to express that an entity got created
                EntityRef entity = entityBuilder.build();

        EntityData.GlobalStore worldData = worldSerializer.serializeWorld(false);
        assertEquals(0, worldData.getEntityCount());
    }

}
