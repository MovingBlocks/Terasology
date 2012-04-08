package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.model.blocks.BlockFamily;

/**
 * Combined with ItemComponent, represents an
 * @author Immortius <immortius@gmail.com>
 */
public final class BlockItemComponent extends AbstractComponent {
    public BlockFamily blockFamily;
    public EntityRef placedEntity = EntityRef.NULL;

    public BlockItemComponent() {}

    public BlockItemComponent(BlockFamily blockFamily) {
        this.blockFamily = blockFamily;
    }
}
