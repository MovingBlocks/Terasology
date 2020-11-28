// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
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
            private final Vector3i next = new Vector3i(region.getMinX(), region.getMinY(), region.getMinZ());

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
                        if (next.z > region.getMaxZ()) {
                            next.z = region.getMinZ();
                            next.y++;
                            if (next.y > region.getMaxY()) {
                                next.y = region.getMinY();
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
                return region.containsBlock(next);
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

        /**
         * iterator where each instance is wrapped by a new vector3i
         * @return
         */
        public Iterable<Vector3i> build() {
            BlockRegionIterable iterable = new BlockRegionIterable(region, subtract);
            return new Iterable<Vector3i>() {
                @NotNull
                @Override
                public Iterator<Vector3i> iterator() {
                    Iterator<Vector3ic> itr = iterable.iterator();
                    return new Iterator<Vector3i>() {
                        @Override
                        public boolean hasNext() {
                            return itr.hasNext();
                        }

                        @Override
                        public Vector3i next() {
                            return new Vector3i(itr.next());
                        }
                    };
                }
            };
        }

        /**
         * iterable with a single vector that is reused to avoid GC
         * @return
         */
        public Iterable<Vector3ic> buildWithReuse() {
            return new BlockRegionIterable(region, subtract);
        }
    }
}
