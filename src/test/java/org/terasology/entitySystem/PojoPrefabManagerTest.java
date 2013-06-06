package org.terasology.entitySystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

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
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PojoPrefabManagerTest {

    public static final String PrefabName = "Test";
    private ComponentLibrary componentLibrary = new ComponentLibraryImpl();
    private PojoPrefabManager prefabManager;

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

    @Test
    public void testListPrefabs() {
        Prefab prefab = prefabManager.createPrefab(PrefabName + "1");
        prefab.setComponent(new StringComponent());
        Prefab prefab2 = prefabManager.createPrefab(PrefabName + "2");
        prefab2.setComponent(new StringComponent());
        Prefab prefab3 = prefabManager.createPrefab(PrefabName + "3");
        prefab3.setComponent(new StringComponent());
        Prefab prefab4 = prefabManager.createPrefab(PrefabName + "4");
        prefab4.setComponent(new LocationComponent());
        Prefab prefab5 = prefabManager.createPrefab(PrefabName + "5");
        prefab5.setComponent(new LocationComponent());

        long i = 0;
        Iterator it = prefabManager.listPrefabs().iterator();
        while (it.hasNext()) {
            i++;
            it.next();
        }
        assertEquals(5, i);

        it = prefabManager.listPrefabs(LocationComponent.class).iterator();
        i = 0;
        while (it.hasNext()) {
            i++;
            it.next();
        }
        assertEquals(2, i);
    }

}
