package org.terasology.components.block;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.model.blocks.BlockFamily;

/**
 * Combined with ItemComponent, represents an
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class BlockItemComponent implements Component {
    public BlockFamily blockFamily;
    public EntityRef placedEntity = EntityRef.NULL;

    public BlockItemComponent() {
    }

    public BlockItemComponent(BlockFamily blockFamily) {
        this.blockFamily = blockFamily;
    }
}
