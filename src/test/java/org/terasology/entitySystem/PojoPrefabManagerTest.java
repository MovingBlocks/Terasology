package org.terasology.entitySystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.junit.Before;
import org.junit.Test;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentLibraryImpl;
import org.terasology.entitySystem.metadata.extension.Quat4fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefabManagerTest {

    public static final String PrefabName = "Test";
    ComponentLibrary componentLibrary = new ComponentLibraryImpl();
    PojoPrefabManager prefabManager;

    @Before
    public void setup() {
        componentLibrary.registerTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        componentLibrary.registerTypeHandler(Quat4f.class, new Quat4fTypeHandler());
        componentLibrary.registerComponentClass(LocationComponent.class);
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
