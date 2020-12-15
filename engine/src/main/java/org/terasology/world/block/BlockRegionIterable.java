// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Iterator;

public class BlockRegionIterable implements Iterable<Vector3ic> {

    private final BlockRegion region;

    BlockRegionIterable(BlockRegion region) {
        this.region = region;
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        return new Iterator<Vector3ic>() {
            private Vector3i current = null;
            private final Vector3i next = new Vector3i(region.getMinX(), region.getMinY(), region.getMinZ());

            public boolean findNext() {
                if (current.equals(next)) {
                    next.z++;
                    if (next.z > region.getMaxZ()) {
                        next.z = region.getMinZ();
                        next.y++;
                        if (next.y > region.getMaxY()) {
                            next.y = region.getMinY();
                            next.x++;
                        }
                    }
                    return region.containsBlock(next);
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
}
