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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.network.NetworkSystem;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 */
public class BaseEntityRefTest {

    private static Context context;
    private PojoEntityManager entityManager;
    //private Prefab prefab;
    private EntityRef ref;

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
        CoreRegistry.setContext(context);
    }

    @Before
    public void setup() {
        context.put(NetworkSystem.class, mock(NetworkSystem.class));
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = (PojoEntityManager) context.get(EntityManager.class);

        ref = entityManager.create();
    }

    @Test
    public void testSetScope() {
        assertEquals(ref.getScope(), EntityData.Entity.Scope.GLOBAL);
        assertTrue(entityManager.getGlobalPool().contains(ref.getId()));

        //Move into sector scope
        ref.setScope(EntityData.Entity.Scope.SECTOR);
        assertEquals(ref.getScope(), EntityData.Entity.Scope.SECTOR);
        assertTrue(entityManager.getSectorManager().contains(ref.getId()));

        //And move back to global scope
        ref.setScope(EntityData.Entity.Scope.GLOBAL);
        assertEquals(ref.getScope(), EntityData.Entity.Scope.GLOBAL);
        assertTrue(entityManager.getGlobalPool().contains(ref.getId()));
    }

}
