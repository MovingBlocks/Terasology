package org.terasology.entitySystem;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.engine.Terasology;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkSystem;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.terasology.testUtil.TeraAssert.assertEqualsContent;

/**
 * @author Immortius
 */
public class OwnershipHelperTest {

    EngineEntityManager entityManager;

    private static ModManager modManager;

    @BeforeClass
    public static void setupClass() {
        modManager = new ModManager();
    }

    @Before
    public void setup() {
        EntitySystemBuilder builder = new EntitySystemBuilder();

        entityManager = builder.build(modManager, mock(NetworkSystem.class));
    }

    @Test
    public void listsOwnedEntities() {
        EntityRef ownedEntity = entityManager.create();
        OwnerComponent ownerComp = new OwnerComponent();
        ownerComp.child = ownedEntity;
        EntityRef ownerEntity = entityManager.create(ownerComp);

        OwnershipHelper helper = new OwnershipHelper(entityManager.getComponentLibrary());
        assertEqualsContent(Lists.newArrayList(ownedEntity), helper.listOwnedEntities(ownerEntity));
    }
}
