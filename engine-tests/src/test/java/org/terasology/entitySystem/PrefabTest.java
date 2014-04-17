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
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.engine.module.ModuleSecurityManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.stubs.OrderedMapTestComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
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
        ModuleManager moduleManager = new ModuleManagerImpl(new ModuleSecurityManager(), false);
        moduleManager.applyActiveModules();
        AssetManager assetManager = new AssetManager(moduleManager);
        CoreRegistry.put(ModuleManager.class, moduleManager);
        CoreRegistry.put(AssetManager.class, assetManager);
        AssetType.registerAssetTypes(assetManager);
        URL url = getClass().getClassLoader().getResource("testResources");
        url = new URL(url.toString().substring(0, url.toString().length() - "testResources".length() - 1));
        assetManager.addAssetSource(new ClasspathSource("unittest", url, TerasologyConstants.ASSETS_SUBDIRECTORY, TerasologyConstants.OVERRIDES_SUBDIRECTORY, TerasologyConstants.DELTAS_SUBDIRECTORY));
        assetManager.setAssetFactory(AssetType.PREFAB, new AssetFactory<PrefabData, Prefab>() {
            @Override
            public Prefab buildAsset(AssetUri uri, PrefabData data) {
                return new PojoPrefab(uri, data);
            }
        });
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        EntityManager em = new EntitySystemBuilder().build(moduleManager, networkSystem, new ReflectionReflectFactory());
        prefabManager = new PojoPrefabManager();
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
    public void prefabHasDefinedComponentsWithOrderedMap() {
        Prefab prefab = prefabManager.getPrefab("unittest:withComponentContainingOrderedMap");
        assertTrue(prefab.hasComponent(OrderedMapTestComponent.class));
        OrderedMapTestComponent component = prefab.getComponent(OrderedMapTestComponent.class);
        assertNotNull(component);
        Map<String, Long> orderedMap = component.orderedMap;
        Set<String> keySet = orderedMap.keySet();
        List<String> keyList = new ArrayList<String>(keySet);
        assertEquals(4, keyList.size());
        assertEquals("one", keyList.get(0));
        assertEquals("two", keyList.get(1));
        assertEquals("three", keyList.get(2));
        assertEquals("four", keyList.get(3));
        assertEquals(Long.valueOf(1), orderedMap.get("one"));
        assertEquals(Long.valueOf(2), orderedMap.get("two"));
        assertEquals(Long.valueOf(3), orderedMap.get("three"));
        assertEquals(Long.valueOf(4), orderedMap.get("four"));
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
