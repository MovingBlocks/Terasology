// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import gnu.trove.list.TIntList;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.world.BlockEntityRegistry;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

//FIXME: There is a mismatch between `blockCount` and iterated blocks.
//       I don't get what this event is about, and how it should behave. The `TIntList positions` is a flattened list
//       of positions. So if this event affects
//          [(0,0,0), (1,2,3), (4,5,6)]
//       then `positions` would be:
//          [0,0,0, 1,2,3, 4,5,6]
//       But whether a block listed in positions is actually returned depends on whether there's a permanent block
//       entity registered or not? Doesn't this lead to a mismatch between the size as returned by blockCount and 
//       the actual amount of positions returned by the iterator? So, it could be that 
//          event.blockCount() == n 
//       but 
//          for (Vector3ic pos: event) { ... }
//       only iterates over m < n blocks? Maybe we should not inline the iterator here, but rather make it explicit
//       that it will only iterator over `blockPositionsWithNonPermanentEntities`.
public abstract class BlockLifecycleEvent implements Event, Iterable<Vector3ic> {
    private TIntList positions;
    private BlockEntityRegistry registry;

    public BlockLifecycleEvent(TIntList positions, BlockEntityRegistry registry) {
        this.registry = registry;
        this.positions = positions;
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        if (positions.size() < 3) {
            return Collections.emptyIterator();
        }
        return new Iterator<Vector3ic>() {
            private Vector3i next = new Vector3i(
                positions.get(0),
                positions.get(1),
                positions.get(2));
            private final Vector3i current = new Vector3i();
            private int index = 3;
            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Vector3ic next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                current.set(next);
                fetchNext();
                return current;
            }

            private void fetchNext() {
                while (index < positions.size() - 2) {
                    next.x = positions.get(index++);
                    next.y = positions.get(index++);
                    next.z = positions.get(index++);
                    if (!registry.hasPermanentBlockEntity(next)) {
                        return;
                    }
                }
                next = null;
            }
        };
    }

    public int blockCount() {
        return positions.size() / 3;
    }
}
