package org.terasology.entitySystem;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.junit.Before;
import org.junit.Test;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.internal.PojoPrefab;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;
import org.terasology.entitySystem.metadata.extension.Quat4fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.internal.PojoPrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;

import static org.junit.Assert.*;

/**
 * @author Immortius <immortius@gmail.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PojoPrefabManagerTest {

    public static final String PREFAB_NAME = "unittest:myprefab";
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
        assertNull(prefabManager.getPrefab(PREFAB_NAME));
    }

    @Test
    public void retrievePrefab() {
        Prefab prefab = new PojoPrefab(new AssetUri(AssetType.PREFAB, PREFAB_NAME), null, true, new StringComponent("Test"));
        prefabManager.registerPrefab(prefab);
        Prefab ref = prefabManager.getPrefab(PREFAB_NAME);
        assertNotNull(ref);
        assertEquals(PREFAB_NAME, ref.getName());
    }


}
