// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.unittest.stubs.StringComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 */
public class PojoPrefabManagerTest {

    public static final String PREFAB_NAME = "unittest:myprefab";
    private PojoPrefabManager prefabManager;

    @BeforeEach
    public void setup() throws Exception {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);
        ModuleManager moduleManager = ModuleManagerFactory.create();

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");

        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        context.put(AssetManager.class, assetTypeManager.getAssetManager());

        prefabManager = new PojoPrefabManager(context);
    }

    @Test
    public void testRetrieveNonExistentPrefab() {
        assertNull(prefabManager.getPrefab(PREFAB_NAME));
    }

    @Test
    public void testRetrievePrefab() {
        PrefabData data = new PrefabData();
        data.addComponent(new StringComponent("Test"));
        Assets.generateAsset(new ResourceUrn(PREFAB_NAME), data, Prefab.class);
        Prefab ref = prefabManager.getPrefab(PREFAB_NAME);
        assertNotNull(ref);
        assertEquals(PREFAB_NAME, ref.getName());
    }


}
