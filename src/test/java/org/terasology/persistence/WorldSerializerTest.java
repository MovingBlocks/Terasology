package org.terasology.persistence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.persistence.serializers.WorldSerializer;
import org.terasology.persistence.serializers.WorldSerializerImpl;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkSystem;
import org.terasology.protobuf.EntityData;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class WorldSerializerTest {

    private EngineEntityManager entityManager;
    private WorldSerializer worldSerializer;
    private static ModManager modManager;

    @BeforeClass
    public static void setupClass() {
        modManager = new ModManager();
    }

    @Before
    public void setup() {

        EntitySystemBuilder builder = new EntitySystemBuilder();
        entityManager = builder.build(modManager, mock(NetworkSystem.class));
        entityManager.getComponentLibrary().register(GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(StringComponent.class);
        entityManager.getComponentLibrary().register(IntegerComponent.class);
        worldSerializer = new WorldSerializerImpl(entityManager);
    }

    @Test
    public void testNotPersistedIfFlagedOtherwise() throws Exception {
        EntityRef entity = entityManager.create();
        entity.setPersistent(false);
        int id = entity.getId();

        EntityData.World worldData = worldSerializer.serializeWorld(false);
        assertEquals(0, worldData.getEntityCount());
        assertEquals(1, worldData.getFreedEntityIdCount());
        assertEquals(id, worldData.getFreedEntityId(0));
    }

}
