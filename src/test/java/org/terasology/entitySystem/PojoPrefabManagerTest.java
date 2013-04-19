package org.terasology.entitySystem;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.junit.Before;
import org.junit.Test;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;
import org.terasology.entitySystem.metadata.extension.Quat4fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Immortius <immortius@gmail.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PojoPrefabManagerTest {

    public static final String PrefabName = "Test";
    private EntitySystemLibrary entitySystemLibrary;
    private ComponentLibrary componentLibrary;
    private PojoPrefabManager prefabManager;

    @Before
    public void setup() {
        TypeHandlerLibrary lib = new TypeHandlerLibraryBuilder()
                .add(Vector3f.class, new Vector3fTypeHandler())
                .add(Quat4f.class, new Quat4fTypeHandler())
                .build();
        entitySystemLibrary = new EntitySystemLibraryImpl(lib);
        componentLibrary = entitySystemLibrary.getComponentLibrary();
        componentLibrary.register(LocationComponent.class);
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
        StringComponent comp = prefab.addComponent(new StringComponent());
        assertNotNull(comp);

        assertEquals(comp, prefab.getComponent(StringComponent.class));
    }


    @Test
    public void testPrefabInheritance() {
        Prefab parentPrefab = prefabManager.createPrefab("parentPrefab");

        LocationComponent testComponent = new LocationComponent();
        parentPrefab.addComponent(testComponent);


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
        prefab.addComponent(new StringComponent());
        Prefab prefab2 = prefabManager.createPrefab(PrefabName + "2");
        prefab2.addComponent(new StringComponent());
        Prefab prefab3 = prefabManager.createPrefab(PrefabName + "3");
        prefab3.addComponent(new StringComponent());
        Prefab prefab4 = prefabManager.createPrefab(PrefabName + "4");
        prefab4.addComponent(new LocationComponent());
        Prefab prefab5 = prefabManager.createPrefab(PrefabName + "5");
        prefab5.addComponent(new LocationComponent());

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
