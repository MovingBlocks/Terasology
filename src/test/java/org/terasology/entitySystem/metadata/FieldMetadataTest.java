package org.terasology.entitySystem.metadata;

import org.junit.Test;
import org.terasology.entitySystem.metadata.core.ListTypeHandler;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.logic.inventory.InventoryComponent;

import static org.junit.Assert.assertTrue;

/**
 * @author Immortius
 */
public class FieldMetadataTest {

    @Test
    public void testOwnsAnnotationProcessed() throws NoSuchFieldException {
        FieldMetadata metadata = new FieldMetadata(OwnerComponent.class.getDeclaredField("child"), new EntityRefTypeHandler(null), false);
        assertTrue(metadata.isOwnedReference());
    }

    @Test
    public void testOwnsAnnotationCollectionProcessed() throws NoSuchFieldException {
        FieldMetadata metadata = new FieldMetadata(InventoryComponent.class.getDeclaredField("itemSlots"), new ListTypeHandler<>(new EntityRefTypeHandler(null)), false);
        assertTrue(metadata.isOwnedReference());
    }

}
