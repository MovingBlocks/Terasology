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
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.stubs.OrderedMapTestComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        ModuleManager moduleManager = ModuleManagerFactory.create();
        CoreRegistry.put(ModuleManager.class, moduleManager);

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.registerCoreAssetType(Prefab.class, PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        CoreRegistry.put(AssetManager.class, assetTypeManager.getAssetManager());

        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        EntityManager em = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
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
