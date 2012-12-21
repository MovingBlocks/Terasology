package org.terasology.world;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

/**
 * @author Immortius
 */
public interface WorldChangeListener {

    public void onBlockChanged(Vector3i pos, Block newBlock, Block originalBlock);

}
