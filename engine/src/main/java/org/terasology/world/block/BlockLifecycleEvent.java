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
package org.terasology.world.block;

import gnu.trove.list.TIntList;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.event.Event;
import org.terasology.world.BlockEntityRegistry;

import java.util.Collections;
import java.util.Iterator;

/**
 */
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
