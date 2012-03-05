package org.terasology.entitySystem;

import org.junit.Test;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.persist.PersisterFactory;
import org.terasology.entitySystem.persist.PrefabPersister;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.entitySystem.stubs.StringComponent;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrefabPersisterTests {

    private PrefabManager manager = new PojoPrefabManager();
    private Prefab prefab;

    public PrefabPersisterTests() {
        this.initializeData();
    }

    private void initializeData() {
        prefab = manager.createPrefab("prefab");

        StringComponent component = new StringComponent();
        component.value = "test";

        prefab.setComponent(component);

        LocationComponent loc = new LocationComponent();

        loc.position.set(1, 1, 1);



        Prefab parentPrefab = manager.createPrefab("parentPrefab");

        parentPrefab.setComponent(loc);

        prefab.addParent(parentPrefab);
    }

    @Test
    public void testPrefabManagerSerializationDeserialization() throws Exception {

        PrefabPersister persister = PersisterFactory.createPrefabPersister();

        StringWriter writer = new StringWriter();

        persister.savePrefabs(writer, manager);
        
        String firstPass = writer.getBuffer().toString();

        StringReader reader = new StringReader(writer.getBuffer().toString());
        PrefabManager deserializedMananger = persister.loadPrefabs(reader);

        // Comparing collections
        Iterator<Prefab> one = manager.listPrefabs().iterator();
        Iterator<Prefab> two = deserializedMananger.listPrefabs().iterator();

        while (one.hasNext() || two.hasNext()) {
            // To ensure both collections is same size
            assertTrue(one.hasNext() && two.hasNext());
            // To ensure both collections elements are equals and in same order
            assertEquals(one.next(), two.next());
        }

        writer = new StringWriter();

        persister.savePrefabs(writer, deserializedMananger);
        
        String secondPass = writer.getBuffer().toString();

        assertEquals(firstPass, secondPass);
    }

    @Test
    public void testPrefabSerializationDeserialization() throws Exception {

        PrefabPersister persister = PersisterFactory.createPrefabPersister();

        StringWriter writer = new StringWriter();

        /** First serialization pass **/
        persister.savePrefab(writer, prefab);
        String firstPass = writer.getBuffer().toString();

        /** Clear space for deserialized prefab **/
        manager.removePrefab(prefab.getName());

        /** Deserialization **/
        StringReader reader = new StringReader(writer.getBuffer().toString());
        Prefab deserializedPrefab = persister.loadPrefab(reader, manager);

        writer = new StringWriter();

        /** Second serialization pass (using deserialized object) **/
        persister.savePrefab(writer, deserializedPrefab);
        String secondPass = writer.getBuffer().toString();

        /** Checks first and second pass results **/
        assertEquals(firstPass, secondPass);

        /** Check serialization results **/
        assertEquals(prefab, deserializedPrefab);
    }
}
