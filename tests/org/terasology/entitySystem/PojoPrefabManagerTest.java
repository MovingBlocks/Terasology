package org.terasology.entitySystem;

import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;

import static org.junit.Assert.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefabManagerTest {

    public static final String PrefabName = "Test";
    PojoPrefabManager prefabManager;

    @Before
    public void setup() {
        prefabManager = new PojoPrefabManager();
    }

    @Test
    public void retrieveNonExistentPrefab() {
        assertNull(prefabManager.get(PrefabName));
    }

    @Test
    public void createPrefab() {
        PrefabRef ref = prefabManager.create(PrefabName);
        assertNotNull(ref);
        assertEquals(PrefabName, ref.getName());
        assertTrue(prefabManager.exists(PrefabName));
    }
    
    @Test
    public void retrievePrefab() {
        prefabManager.create(PrefabName);
        PrefabRef ref = prefabManager.get(PrefabName);
        assertNotNull(ref);
        assertEquals(PrefabName, ref.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void errorIfCreatePrefabWithUsedName() {
        prefabManager.create(PrefabName);
        prefabManager.create(PrefabName);
    }

    @Test
    public void renamePrefab() {
        PrefabRef ref = prefabManager.create(PrefabName);
        ref.rename("NewName");

        assertEquals("NewName", ref.getName());
        assertNotNull(prefabManager.get("NewName"));
        assertNull(prefabManager.get(PrefabName));
        assertTrue(prefabManager.exists("NewName"));
        assertFalse(prefabManager.exists(PrefabName));
    }
    
    @Test
    public void destroyPrefab() {
        PrefabRef ref = prefabManager.create(PrefabName);
        ref.destroy();
        assertFalse(prefabManager.exists(PrefabName));
    }
    
    @Test
    public void prefabRefEquals() {
        prefabManager.create(PrefabName);
        assertEquals(prefabManager.get(PrefabName), prefabManager.get(PrefabName));
    }
    
    @Test
    public void addAndRetrieveComponent() {
        PrefabRef prefab = prefabManager.create(PrefabName);
        StringComponent comp = prefab.addComponent(new StringComponent());
        assertNotNull(comp);

        assertEquals(comp, prefab.getComponent(StringComponent.class));
    }


}
