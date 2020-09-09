// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.internal;

import gnu.trove.list.TIntList;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.math.geom.Vector3i;

import java.util.Iterator;

/**
 *
 */
public class BlockPositionIterator implements Iterator<Vector3i> {
    private final BlockEntityRegistry registry;
    private final TIntList positionList;
    private int i;
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
