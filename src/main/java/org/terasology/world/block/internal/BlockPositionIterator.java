package org.terasology.world.block.internal;

import gnu.trove.list.TIntList;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;

import java.util.Iterator;

/**
 * @author Immortius
 */
public class BlockPositionIterator implements Iterator<Vector3i> {
    private BlockEntityRegistry registry;
    private TIntList positionList;
    private int i = 0;
    private Vector3i nextResult = new Vector3i();

    public BlockPositionIterator(TIntList positionList, BlockEntityRegistry registry) {
        this.positionList = positionList;
        this.registry = registry;
        iterate();
    }

    @Override
    public boolean hasNext() {
        return nextResult != null;
    }

    @Override
    public Vector3i next() {
        Vector3i result = new Vector3i(nextResult);
        iterate();

        return result;
    }

    private void iterate() {
        while (i < positionList.size() - 2) {
            nextResult.x = positionList.get(i++);
            nextResult.y = positionList.get(i++);
            nextResult.z = positionList.get(i++);
            if (!registry.hasPermanentBlockEntity(nextResult)) {
                return;
            }
        }
        nextResult = null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported on BlockPositionIterator");
    }
}
