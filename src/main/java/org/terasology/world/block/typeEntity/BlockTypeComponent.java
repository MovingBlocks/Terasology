package org.terasology.world.block.typeEntity;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.world.block.Block;

/**
 * @author Immortius
 */
public class BlockTypeComponent implements Component {
    @Replicate
    public Block block;
}
