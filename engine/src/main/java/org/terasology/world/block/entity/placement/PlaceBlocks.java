package org.terasology.world.block.entity.placement;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

import java.util.Collections;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PlaceBlocks extends AbstractConsumableEvent {
    private Map<Vector3i, Block> blocks;
    private EntityRef instigator;

    public PlaceBlocks(Vector3i location, Block block) {
        this(location, block, EntityRef.NULL);
    }

    public PlaceBlocks(Map<Vector3i, Block> blocks) {
        this(blocks, EntityRef.NULL);
    }

    public PlaceBlocks(Vector3i location, Block block, EntityRef instigator) {
        blocks = Collections.singletonMap(location, block);
        this.instigator = instigator;
    }

    public PlaceBlocks(Map<Vector3i, Block> blocks, EntityRef instigator) {
        this.blocks = blocks;
        this.instigator = instigator;
    }

    public Map<Vector3i, Block> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
