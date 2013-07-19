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
import org.terasology.entitySystem.internal.PojoPrefabManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.logic.mod.ModManager;
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
        ModManager modManager = new ModManager();
        modManager.applyActiveMods();
        CoreRegistry.put(ModManager.class, modManager);
        AssetType.registerAssetTypes();
        URL url = getClass().getClassLoader().getResource("testResources");
        url = new URL(url.toString().substring(0, url.toString().length() - "testResources".length() - 1));
        AssetManager.getInstance().addAssetSource(new ClasspathSource("unittest", url, ModManager.ASSETS_SUBDIRECTORY, ModManager.OVERRIDES_SUBDIRECTORY));
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        EntityManager em = new EntitySystemBuilder().build(modManager, networkSystem);
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
