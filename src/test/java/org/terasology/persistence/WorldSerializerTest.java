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
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.classMetadata.reflect.ReflectionReflectFactory;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.persistence.serializers.WorldSerializer;
import org.terasology.persistence.serializers.WorldSerializerImpl;
import org.terasology.protobuf.EntityData;
import org.terasology.engine.SimpleUri;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class WorldSerializerTest {

    private static ModuleManager moduleManager;

    private EngineEntityManager entityManager;
    private WorldSerializer worldSerializer;

    @BeforeClass
    public static void setupClass() {
        moduleManager = new ModuleManager();
    }

    @Before
    public void setup() {

        EntitySystemBuilder builder = new EntitySystemBuilder();
        entityManager = builder.build(moduleManager, mock(NetworkSystem.class), new ReflectionReflectFactory());
        entityManager.getComponentLibrary().register(new SimpleUri("test", "gettersetter"), GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(new SimpleUri("test", "string"), StringComponent.class);
        entityManager.getComponentLibrary().register(new SimpleUri("test", "integer"), IntegerComponent.class);
        worldSerializer = new WorldSerializerImpl(entityManager, new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary()));
    }

    @Test
    public void testNotPersistedIfFlagedOtherwise() throws Exception {
        EntityRef entity = entityManager.create();
        entity.setPersistent(false);
        int id = entity.getId();

        EntityData.GlobalStore worldData = worldSerializer.serializeWorld(false);
        assertEquals(0, worldData.getEntityCount());
        assertEquals(1, worldData.getFreedEntityIdCount());
        assertEquals(id, worldData.getFreedEntityId(0));
    }

}
