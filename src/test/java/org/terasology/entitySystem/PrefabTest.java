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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.internal.PojoPrefabManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Immortius
 */
public class PrefabTest {

    private static final Logger logger = LoggerFactory.getLogger(PrefabTest.class);

    private PrefabManager prefabManager;

    @Before
    public void setup() throws Exception {
        ModuleManager moduleManager = new ModuleManager();
        moduleManager.applyActiveModules();
        CoreRegistry.put(ModuleManager.class, moduleManager);
        AssetType.registerAssetTypes();
        URL url = getClass().getClassLoader().getResource("testResources");
        url = new URL(url.toString().substring(0, url.toString().length() - "testResources".length() - 1));
        AssetManager.getInstance().addAssetSource(new ClasspathSource("unittest", url, ModuleManager.ASSETS_SUBDIRECTORY, ModuleManager.OVERRIDES_SUBDIRECTORY));
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        EntityManager em = new EntitySystemBuilder().build(moduleManager, networkSystem);
        prefabManager = new PojoPrefabManager(em.getComponentLibrary());

        for (AssetUri prefabUri : AssetManager.getInstance().listAssets(AssetType.PREFAB)) {
            prefabManager.registerPrefab(Assets.get(prefabUri, Prefab.class));
        }
    }

    @Test
    public void getSimplePrefab() {
        Prefab prefab = prefabManager.getPrefab("unittest:simple");
        assertNotNull(prefab);
        assertEquals("unittest:simple", prefab.getName());
    }

    @Test
    public void prefabHasDefinedComponents() {
        Prefab prefab = prefabManager.getPrefab("unittest:withComponent");
        assertTrue(prefab.hasComponent(StringComponent.class));
    }

    @Test
    public void prefabInheritsFromParent() {
        Prefab prefab = prefabManager.getPrefab("unittest:inheritsComponent");
        assertTrue(prefab.hasComponent(StringComponent.class));
    }

    @Test
    public void prefabTransitiveInheritance() {
        Prefab prefab = prefabManager.getPrefab("unittest:multilevelInheritance");
        assertTrue(prefab.hasComponent(StringComponent.class));
    }
}
