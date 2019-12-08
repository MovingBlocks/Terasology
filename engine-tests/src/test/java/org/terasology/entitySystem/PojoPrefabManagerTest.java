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
package org.terasology.entitySystem;

import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.mathTypes.Quat4fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fTypeHandler;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.utilities.Assets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 */
public class PojoPrefabManagerTest {

    public static final String PREFAB_NAME = "unittest:myprefab";
    private EntitySystemLibrary entitySystemLibrary;
    private ComponentLibrary componentLibrary;
    private PojoPrefabManager prefabManager;

    @Before
    public void setup() throws Exception {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);
        ModuleManager moduleManager = ModuleManagerFactory.create();

        Reflections reflections = new Reflections(getClass().getClassLoader());
        TypeHandlerLibrary lib = new TypeHandlerLibrary(reflections);

        lib.addTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        lib.addTypeHandler(Quat4f.class, new Quat4fTypeHandler());

        entitySystemLibrary = new EntitySystemLibrary(context, lib);
        componentLibrary = entitySystemLibrary.getComponentLibrary();

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.registerCoreAssetType(Prefab.class,
                (AssetFactory<Prefab, PrefabData>) PojoPrefab::new, "prefabs");

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
