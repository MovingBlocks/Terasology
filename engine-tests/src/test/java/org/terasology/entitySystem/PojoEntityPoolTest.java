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
package org.terasology.entitySystem;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.entity.internal.PojoEntityPool;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.network.NetworkSystem;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 */
public class PojoEntityPoolTest {

    private PojoEntityPool pool;
    private static Context context;
    private PojoEntityManager entityManager;

    @BeforeClass
    public static void setupClass() throws Exception {
        context = new ContextImpl();
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.registerCoreAssetType(Prefab.class,
                (AssetFactory<Prefab, PrefabData>) PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        CoreRegistry.setContext(context);
    }

    @Before
    public void setup() {
        context.put(NetworkSystem.class, mock(NetworkSystem.class));
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = (PojoEntityManager) context.get(EntityManager.class);

        pool = new PojoEntityPool(entityManager);
    }


    @Test
    public void testContains() {
        assertFalse(pool.contains(PojoEntityManager.NULL_ID));
        assertFalse(pool.contains(1000000));
        EntityRef ref = entityManager.create();
        entityManager.moveToPool(ref.getId(), pool);
        assertTrue(pool.contains(ref.getId()));
    }

    @Test
    public void testRemove() {
        EntityRef ref = entityManager.create();
        entityManager.moveToPool(ref.getId(), pool);
        assertTrue(pool.contains(ref.getId()));

        pool.remove(ref.getId());
        assertFalse(pool.contains(ref.getId()));
    }

}
