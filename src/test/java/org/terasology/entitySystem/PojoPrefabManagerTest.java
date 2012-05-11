package org.terasology.entitySystem;

import org.junit.Before;
import org.junit.Test;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentLibraryImpl;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;

import javax.vecmath.Vector3f;

import static org.junit.Assert.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefabManagerTest {

    public static final String PrefabName = "Test";
    ComponentLibrary componentLibrary = new ComponentLibraryImpl();
    PojoPrefabManager prefabManager;

    @Before
    public void setup() {
        prefabManager = new PojoPrefabManager(componentLibrary);
    }

    @Test
    public void retrieveNonExistentPrefab() {
        assertNull(prefabManager.getPrefab(PrefabName));
    }

    @Test
    public void createPrefab() {
        Prefab ref = prefabManager.createPrefab(PrefabName);
        assertNotNull(ref);
        assertEquals(PrefabName, ref.getName());
        assertTrue(prefabManager.exists(PrefabName));
    }
    
    @Test
    public void retrievePrefab() {
        prefabManager.createPrefab(PrefabName);
        Prefab ref = prefabManager.getPrefab(PrefabName);
        assertNotNull(ref);
        assertEquals(PrefabName, ref.getName());
    }

    @Test
    public void errorIfCreatePrefabWithUsedName() {
        assertEquals(prefabManager.createPrefab(PrefabName), prefabManager.createPrefab(PrefabName));
    }

    @Test
    public void prefabRefEquals() {
        prefabManager.createPrefab(PrefabName);

        assertEquals(prefabManager.getPrefab(PrefabName), prefabManager.getPrefab(PrefabName));
    }
    
    @Test
    public void addAndRetrieveComponent() {
        Prefab prefab = prefabManager.createPrefab(PrefabName);
        StringComponent comp = prefab.setComponent(new StringComponent());
        assertNotNull(comp);

        assertEquals(comp, prefab.getComponent(StringComponent.class));
    }


    @Test
    public void testPrefabInheritance() {
        Prefab parentPrefab = prefabManager.createPrefab("parentPrefab");

        LocationComponent testComponent = new LocationComponent();
        parentPrefab.setComponent(testComponent);


        Prefab prefab = prefabManager.createPrefab(PrefabName);
        prefab.addParent(parentPrefab);

        assertEquals(testComponent, prefab.getComponent(testComponent.getClass()));

        prefab.getComponent(testComponent.getClass()).getLocalPosition().set(1, 1, 1);

        assertNotSame(testComponent.getLocalPosition(),  prefab.getComponent(testComponent.getClass()).getLocalPosition());
        assertEquals(new Vector3f(0,0,0), testComponent.getLocalPosition());
    }

}
