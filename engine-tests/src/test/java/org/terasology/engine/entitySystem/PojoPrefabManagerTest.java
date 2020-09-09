// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.engine.entitySystem.stubs.StringComponent;
import org.terasology.gestalt.assets.AssetFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.engine.persistence.typeHandling.mathTypes.legacy.LegacyQuat4fTypeHandler;
import org.terasology.engine.persistence.typeHandling.mathTypes.legacy.LegacyVector3fTypeHandler;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.engine.utilities.Assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 */
public class PojoPrefabManagerTest {

    public static final String PREFAB_NAME = "unittest:myprefab";
    private EntitySystemLibrary entitySystemLibrary;
    private ComponentLibrary componentLibrary;
    private PojoPrefabManager prefabManager;

    @BeforeEach
    public void setup() throws Exception {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);
        ModuleManager moduleManager = ModuleManagerFactory.create();

        Reflections reflections = new Reflections(getClass().getClassLoader());
        TypeHandlerLibrary lib = new TypeHandlerLibrary(reflections);

        lib.addTypeHandler(Vector3f.class, new LegacyVector3fTypeHandler());
        lib.addTypeHandler(Quat4f.class, new LegacyQuat4fTypeHandler());

        entitySystemLibrary = new EntitySystemLibrary(context, lib);
        componentLibrary = entitySystemLibrary.getComponentLibrary();

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.createAssetType(Prefab.class,
                PojoPrefab::new, "prefabs");

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
        Prefab prefab = Assets.generateAsset(new ResourceUrn(PREFAB_NAME), data, Prefab.class);
        Prefab ref = prefabManager.getPrefab(PREFAB_NAME);
        assertNotNull(ref);
        assertEquals(PREFAB_NAME, ref.getName());
    }


}
