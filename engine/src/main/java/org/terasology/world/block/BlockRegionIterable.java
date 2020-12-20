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
            private final Vector3i next = region.getMin(new Vector3i());

            public boolean findNext() {
                if (current.equals(next)) {
                    next.z++;
                    if (next.z > region.maxZ()) {
                        next.z = region.minZ();
                        next.y++;
                        if (next.y > region.maxY()) {
                            next.y = region.minY();
                            next.x++;
                        }
                    }
                    return region.contains(next);
                }
                return true;
            }

            @Override
            public boolean hasNext() {
                if (!region.isValid()) {
                    return false;
                }
                if (current == null) {
                    return true;
                }

                if (current.equals(next)) {
                    return findNext();
                }
                return region.contains(next);
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
