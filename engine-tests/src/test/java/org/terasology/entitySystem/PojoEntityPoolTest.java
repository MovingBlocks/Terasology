// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityPool;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PojoEntityPoolTest {

    private PojoEntityPool pool;
    private static Context context;
    private PojoEntityManager entityManager;

    @BeforeAll
    public static void setupClass() throws Exception {
        context = new ContextImpl();
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.createAssetType(Prefab.class,
                PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        CoreRegistry.setContext(context);
    }

    @BeforeEach
    public void setup() {
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        context.put(NetworkSystem.class, networkSystem);
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
