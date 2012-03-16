package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.model.blocks.BlockGroup;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class BlockItemComponent extends AbstractComponent {
    public BlockGroup blockGroup;

    public BlockItemComponent() {}

    public BlockItemComponent(BlockGroup blockGroup) {
        this.blockGroup = blockGroup;
    }
}
