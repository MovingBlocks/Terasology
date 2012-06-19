package org.terasology.components;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

import java.util.List;

/**
 * Allows an entity to store items
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class InventoryComponent implements Component {

    public List<EntityRef> itemSlots = Lists.newArrayList();

    public InventoryComponent() {
    }

    public InventoryComponent(int numSlots) {
        for (int i = 0; i < numSlots; ++i) {
            itemSlots.add(EntityRef.NULL);
        }
    }
}
