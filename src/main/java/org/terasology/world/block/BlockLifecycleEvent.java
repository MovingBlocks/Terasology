package org.terasology.world.block;

import gnu.trove.list.TIntList;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.internal.BlockPositionIterator;

import java.util.Iterator;

/**
 * @author Immortius
 */
public abstract class BlockLifecycleEvent implements Event, Iterable<Vector3i> {
    private TIntList positions;
    private BlockEntityRegistry registry;

    public BlockLifecycleEvent(TIntList positions, BlockEntityRegistry registry) {
        this.registry = registry;
        this.positions = positions;
    }

    @Override
    public Iterator<Vector3i> iterator() {
        return new BlockPositionIterator(positions, registry);
    }

    public int size() {
        return positions.size();
    }
}
