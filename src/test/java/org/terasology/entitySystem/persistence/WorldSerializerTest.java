package org.terasology.entitySystem.persistence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.logic.mod.ModManager;
import org.terasology.protobuf.EntityData;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class WorldSerializerTest {

    private PersistableEntityManager entityManager;
    private WorldSerializer worldSerializer;
    private static ModManager modManager;

    @BeforeClass
    public static void setupClass() {
        modManager = new ModManager();
    }

    @Before
    public void setup() {

        EntitySystemBuilder builder = new EntitySystemBuilder();
        entityManager = builder.build(modManager);
        entityManager.getComponentLibrary().register(GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(StringComponent.class);
        entityManager.getComponentLibrary().register(IntegerComponent.class);
        worldSerializer = new WorldSerializerImpl(entityManager);
    }

    @Test
    public void testNotPersistedIfFlagedOtherwise() throws Exception {
        EntityRef entity = entityManager.create();
        entity.setPersisted(false);
        int id = entity.getId();

        EntityData.World worldData = worldSerializer.serializeWorld(false);
        assertEquals(0, worldData.getEntityCount());
        assertEquals(1, worldData.getFreedEntityIdCount());
        assertEquals(id, worldData.getFreedEntityId(0));
    }

}
