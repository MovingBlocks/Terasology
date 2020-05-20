/*
 * Copyright 2020 MovingBlocks
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

import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Iterator;
import java.util.List;

public class BlockRegionIterable implements Iterable<Vector3ic> {

    private final BlockRegion region;
    private List<BlockRegion> subtract;

    private BlockRegionIterable(BlockRegion region, List<BlockRegion> subtract) {
        this.region = region;
        this.subtract = subtract;
    }

    public static BlockRegionIterable.Builder region(BlockRegion region) {
        return new BlockRegionIterable.Builder(region);
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        return new Iterator<Vector3ic>() {
            private Vector3i current = null;
            private final Vector3i next = new Vector3i(region.aabb.minX, region.aabb.minY, region.aabb.minZ);

            public boolean isValid(Vector3ic test) {
                if (subtract != null) {
                    for (BlockRegion blockRegion : subtract) {
                        if (blockRegion.containsBlock(test)) {
                            return false;
                        }
                    }
                }
                return true;
            }

            public boolean findNext() {
                if (current.equals(next)) {
                    do {
                        next.z++;
                        if (next.z >= region.aabb.maxZ) {
                            next.z = region.aabb.minZ;
                            next.y++;
                            if (next.y >= region.aabb.maxY) {
                                next.y = region.aabb.minY;
                                next.x++;
                            }
                        }
                        if (!region.containsBlock(next)) {
                            return false;
                        }
                    } while (!isValid(next));
                    return true;
                }
                return true;
            }

            @Override
            public boolean hasNext() {
                if (current == null) {
                    return true;
                }

                if (current.equals(next)) {
                    return findNext();
                }
                return !region.containsBlock(next);
            }

            @Override
            public Vector3ic next() {
                if (current == null) {
                    current = new Vector3i(next);
                    return next;
                }

                if (current.equals(next)) {
                    if (findNext()) {
                        return next;
                    }
                    return null;
                }
                current.set(next);
                return next;
            }
        };
    }

    public static final class Builder {
        private final BlockRegion region;
        private List<BlockRegion> subtract;

        private Builder(BlockRegion region) {
            this.region = region;
        }

        public BlockRegionIterable.Builder subtract(BlockRegion reg) {
            if (subtract == null) {
                subtract = Lists.newArrayList();
            }
            subtract.add(reg);
            return this;
        }

        public BlockRegionIterable build() {
            return new BlockRegionIterable(region, subtract);
        }
    }
}
