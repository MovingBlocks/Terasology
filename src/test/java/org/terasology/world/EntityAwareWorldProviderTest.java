/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.world;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.logic.mod.ModManager;
import org.terasology.testUtil.WorldProviderCoreStub;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.BlockComponent;
import org.terasology.world.block.BlockEntityMode;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Immortius
 */
public class EntityAwareWorldProviderTest {

    public static final String PREFAB_URI = "unittest:blockprefab";
    private EntityAwareWorldProvider worldProvider;
    private PrefabManager prefabManager;
    private EntityManager entityManager;
    private static ModManager modManager;
    private BlockManager blockManager;

    @BeforeClass
    public static void commonSetup() {
        modManager = new ModManager();
    }

    @Before
    public void setup() {
        EntitySystemBuilder builder = new EntitySystemBuilder();
        blockManager = CoreRegistry.put(BlockManager.class, new BlockManager());

        entityManager = builder.build(modManager);
        prefabManager = entityManager.getPrefabManager();
        worldProvider = new EntityAwareWorldProvider(new WorldProviderCoreStub(BlockManager.getAir()));
        worldProvider.entityManager = entityManager;
    }

    @Test
    public void testAddBlockWithPersistentEntity() {

        Prefab prefab = prefabManager.createPrefab(PREFAB_URI);
        prefab.setComponent(new StringComponent());

        Block persistentEntityBlock = new Block();
        persistentEntityBlock.setEntityMode(BlockEntityMode.PERSISTENT);
        persistentEntityBlock.setEntityPrefab(PREFAB_URI);
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("unittest:block"), persistentEntityBlock));

        assertTrue(worldProvider.setBlock(0, 0, 0, persistentEntityBlock, BlockManager.getAir()));
        List<EntityRef> blockEntities = Lists.newArrayList(entityManager.iteratorEntities(BlockComponent.class));
        assertEquals(1, blockEntities.size());
        assertNotNull(blockEntities.get(0).getComponent(StringComponent.class));
        assertNotNull(blockEntities.get(0).getComponent(BlockComponent.class));
        assertFalse(blockEntities.get(0).getComponent(BlockComponent.class).temporary);
    }

    @Test
    public void testAddBlockWithPlacedEntity() {

        Prefab prefab = prefabManager.createPrefab(PREFAB_URI);
        prefab.setComponent(new StringComponent());

        Block persistentEntityBlock = new Block();
        persistentEntityBlock.setEntityMode(BlockEntityMode.WHILE_PLACED);
        persistentEntityBlock.setEntityPrefab(PREFAB_URI);
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("unittest:block"), persistentEntityBlock));

        assertTrue(worldProvider.setBlock(0, 0, 0, persistentEntityBlock, BlockManager.getAir()));
        List<EntityRef> blockEntities = Lists.newArrayList(entityManager.iteratorEntities(BlockComponent.class));
        assertEquals(1, blockEntities.size());
        assertNotNull(blockEntities.get(0).getComponent(StringComponent.class));
        assertNotNull(blockEntities.get(0).getComponent(BlockComponent.class));
        assertFalse(blockEntities.get(0).getComponent(BlockComponent.class).temporary);
    }

    @Test
    public void testAddBlockWithExistingEntity() {

        Prefab prefab = prefabManager.createPrefab(PREFAB_URI);
        prefab.setComponent(new StringComponent());

        Block persistentEntityBlock = new Block();
        persistentEntityBlock.setEntityMode(BlockEntityMode.PERSISTENT);
        persistentEntityBlock.setEntityPrefab(PREFAB_URI);
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("unittest:block"), persistentEntityBlock));

        EntityRef entity = entityManager.create(prefab);
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "hi";
        entity.saveComponent(comp);

        assertTrue(worldProvider.setBlock(0, 0, 0, persistentEntityBlock, BlockManager.getAir(), entity));
        List<EntityRef> blockEntities = Lists.newArrayList(entityManager.iteratorEntities(BlockComponent.class));
        assertEquals(1, blockEntities.size());
        assertEquals(blockEntities.get(0), entity);
        assertNotNull(blockEntities.get(0).getComponent(StringComponent.class));
        assertEquals("hi", blockEntities.get(0).getComponent(StringComponent.class).value);
        assertNotNull(blockEntities.get(0).getComponent(BlockComponent.class));
        assertFalse(blockEntities.get(0).getComponent(BlockComponent.class).temporary);
    }

    @Test
    public void testAddBlockWithPreExistingEntity() {

    }
}
