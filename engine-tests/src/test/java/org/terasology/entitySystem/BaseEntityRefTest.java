// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.entitySystem;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.MockedPathManager;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.entitySystem.entity.internal.EntityScope;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.network.NetworkSystem;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.terasology.entitySystem.entity.internal.EntityScope.CHUNK;
import static org.terasology.entitySystem.entity.internal.EntityScope.GLOBAL;
import static org.terasology.entitySystem.entity.internal.EntityScope.SECTOR;

public class BaseEntityRefTest implements MockedPathManager {

    private Context context;
    private PojoEntityManager entityManager;
    private EntityRef ref;

    @BeforeEach
    public void setupClass() throws Exception {
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
        context.put(NetworkSystem.class, mock(NetworkSystem.class));
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = (PojoEntityManager) context.get(EntityManager.class);

        ref = entityManager.create();
    }

    @Test
    public void testSetScope() {
        ref = entityManager.create();
        assertEquals(CHUNK, ref.getScope());
        for (EntityScope scope : EntityScope.values()) {
            ref.setScope(scope);
            assertEquals(ref.getScope(), scope);
        }

        //Move into sector scope
        ref.setScope(SECTOR);
        assertEquals(ref.getScope(), SECTOR);
        assertTrue(entityManager.getSectorManager().contains(ref.getId()));
        assertFalse(entityManager.getGlobalPool().contains(ref.getId()));

        //And move back to global scope
        ref.setScope(GLOBAL);
        assertEquals(ref.getScope(), GLOBAL);
        assertTrue(entityManager.getGlobalPool().contains(ref.getId()));
        assertFalse(entityManager.getSectorManager().contains(ref.getId()));

    }

    @Test
    public void testCreateWithScopeInfoComponent() {
        EntityInfoComponent info = new EntityInfoComponent();
        info.scope = SECTOR;
        EntityInfoComponent info2 = new EntityInfoComponent();
        info2.scope = SECTOR;

        ref = entityManager.create(info);
        assertEquals(SECTOR, ref.getScope());

        long safeId = ref.getId();
        ref.destroy();

        ref = entityManager.createEntityWithId(safeId, Lists.newArrayList(info2));
        assertNotNull(ref);
        assertNotEquals(EntityRef.NULL, ref);
        assertEquals(SECTOR, ref.getScope());

    }

}
