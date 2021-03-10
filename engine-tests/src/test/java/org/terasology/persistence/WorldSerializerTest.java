/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.engine.persistence;

import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializerImpl;
import org.terasology.protobuf.EntityData;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
