// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.engine.entitySystem.prefab.internal.PrefabFormat;
import org.terasology.unittest.stubs.ListOfEnumsComponent;
import org.terasology.unittest.stubs.ListOfObjectComponent;
import org.terasology.unittest.stubs.MappedContainerComponent;
import org.terasology.unittest.stubs.OrderedMapTestComponent;
import org.terasology.unittest.stubs.StringComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.engine.testUtil.ModuleManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrefabTest {
    private PrefabManager prefabManager;

    @BeforeEach
    public void setup() throws Exception {
        ContextImpl context = new ContextImpl();
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        CoreRegistry.setContext(context);
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        AssetType<Prefab, PrefabData> prefabDataAssetType = assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");
        ComponentLibrary componentLibrary = context.get(ComponentLibrary.class);
        TypeHandlerLibrary typeHandlerLibrary = context.get(TypeHandlerLibrary.class);
        PrefabFormat prefabFormat = new PrefabFormat(componentLibrary, typeHandlerLibrary);
        assetTypeManager.getAssetFileDataProducer(prefabDataAssetType).addAssetFormat(prefabFormat);
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        context.put(AssetManager.class, assetTypeManager.getAssetManager());

        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        context.put(NetworkSystem.class, networkSystem);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);

        prefabManager = new PojoPrefabManager(context);
    }

    @Test
    public void testGetSimplePrefab() {
        Prefab prefab = prefabManager.getPrefab("unittest:simple");
        assertNotNull(prefab);
        assertEquals("unittest:simple", prefab.getName());
    }

    @Test
    public void testPrefabHasDefinedComponents() {
        Prefab prefab = prefabManager.getPrefab("unittest:withComponent");
        assertTrue(prefab.hasComponent(StringComponent.class));
    }

    @Test
    public void testPrefabHasDefinedComponentsWithOrderedMap() {
        Prefab prefab = prefabManager.getPrefab("unittest:withComponentContainingOrderedMap");
        assertTrue(prefab.hasComponent(OrderedMapTestComponent.class));
        OrderedMapTestComponent component = prefab.getComponent(OrderedMapTestComponent.class);
        assertNotNull(component);
        Map<String, Long> orderedMap = component.orderedMap;
        Set<String> keySet = orderedMap.keySet();
        List<String> keyList = new ArrayList<>(keySet);
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
    public void testPrefabInheritsFromParent() {
        Prefab prefab = prefabManager.getPrefab("unittest:inheritsComponent");
        assertTrue(prefab.hasComponent(StringComponent.class));
    }

    @Test
    public void testPrefabTransitiveInheritance() {
        Prefab prefab = prefabManager.getPrefab("unittest:multilevelInheritance");
        assertTrue(prefab.hasComponent(StringComponent.class));
    }

    @Test
    public void testPrefabWithCollectionOfMappedContainers() {
        Prefab prefab = prefabManager.getPrefab("unittest:withCollectionOfMappedContainers");
        MappedContainerComponent mappedContainer = prefab.getComponent(MappedContainerComponent.class);
        assertNotNull(mappedContainer);
        assertNotNull(mappedContainer.containers);
        assertEquals(1, mappedContainer.containers.size());
        MappedContainerComponent.Cont cont = mappedContainer.containers.iterator().next();
        assertNotNull(cont);
        assertEquals("a", cont.value);
    }

    @Test
    public void testPrefabWithListOfMappedContainers() {
        Prefab prefab = prefabManager.getPrefab("unittest:withListContainer");
        ListOfObjectComponent mappedContainer = prefab.getComponent(ListOfObjectComponent.class);
        assertEquals(2, mappedContainer.elements.size());
        assertEquals("returnHome", mappedContainer.elements.get(1).id);
    }


    @Test
    public void testPrefabWithEmptyListOfMappedContainers() {
        Prefab prefab = prefabManager.getPrefab("unittest:withEmptyListContainer");
        ListOfObjectComponent mappedContainer = prefab.getComponent(ListOfObjectComponent.class);
        assertEquals(0, mappedContainer.elements.size());
    }

    @Test
    public void testPrefabWithListOfEnums() {
        Prefab prefab = prefabManager.getPrefab("unittest:withListEnumContainer");
        ListOfEnumsComponent mappedContainer = prefab.getComponent(ListOfEnumsComponent.class);
        assertEquals(6, mappedContainer.elements.size());
        assertEquals(Side.TOP, mappedContainer.elements.get(0));
        assertEquals(Side.LEFT, mappedContainer.elements.get(1));
        assertEquals(Side.RIGHT, mappedContainer.elements.get(2));
        assertEquals(Side.FRONT, mappedContainer.elements.get(3));
        assertEquals(Side.BACK, mappedContainer.elements.get(4));
        assertEquals(Side.BOTTOM, mappedContainer.elements.get(5));

    }
}
