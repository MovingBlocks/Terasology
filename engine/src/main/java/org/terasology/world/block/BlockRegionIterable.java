// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
                return current;
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
