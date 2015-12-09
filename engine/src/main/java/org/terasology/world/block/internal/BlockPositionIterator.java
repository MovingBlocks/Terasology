/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block.internal;

import gnu.trove.list.TIntList;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;

import java.util.Iterator;

/**
 */
public class BlockPositionIterator implements Iterator<Vector3i> {
    private BlockEntityRegistry registry;
    private TIntList positionList;
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
