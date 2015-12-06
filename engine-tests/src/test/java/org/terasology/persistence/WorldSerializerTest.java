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
package org.terasology.persistence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.management.AssetManager;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.persistence.serializers.WorldSerializer;
import org.terasology.persistence.serializers.WorldSerializerImpl;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 */
public class WorldSerializerTest extends TerasologyTestingEnvironment {

    @Test
    public void testNotPersistedIfFlagedOtherwise() throws Exception {
        EngineEntityManager entityManager = context.get(EngineEntityManager.class);
        EntityBuilder entityBuilder = entityManager.newBuilder();
        WorldSerializer worldSerializer = new WorldSerializerImpl(entityManager, new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary()));
        entityBuilder.setPersistent(false);
        @SuppressWarnings("unused") // just used to express that an entity got created
                EntityRef entity = entityBuilder.build();

        EntityData.GlobalStore worldData = worldSerializer.serializeWorld(false);
        assertEquals(0, worldData.getEntityCount());
    }

}
