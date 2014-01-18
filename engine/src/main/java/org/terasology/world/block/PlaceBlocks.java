package org.terasology.world.block;

import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.math.Vector3i;

import java.util.Collections;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PlaceBlocks extends AbstractConsumableEvent {
    private Map<Vector3i, Block> blocks;

    public PlaceBlocks(Vector3i location, Block block) {
        blocks = Collections.singletonMap(location, block);
    }

    public PlaceBlocks(Map<Vector3i, Block> blocks) {
        this.blocks = blocks;
    }

    public Map<Vector3i, Block> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }
}
