package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.model.blocks.BlockGroup;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class PlaceableBlockComponent extends AbstractComponent {
    public BlockGroup blockGroup;

    public PlaceableBlockComponent() {}

    public PlaceableBlockComponent(BlockGroup blockGroup) {
        this.blockGroup = blockGroup;
    }
}
