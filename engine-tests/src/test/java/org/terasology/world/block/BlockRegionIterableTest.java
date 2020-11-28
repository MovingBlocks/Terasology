// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BlockRegionIterableTest {

    @Test
    public void testSingleBlockRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        List<Vector3ic> actual = new ArrayList<>();
        for (Vector3ic vector3ic : iterable) {
            actual.add(new Vector3i(vector3ic));
        }

        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(new HashSet<>(expectedPositions(region)), new HashSet<>(actual));
    }

    @Test
    public void testLineOfBlocksRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 1, 0));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        List<Vector3ic> actual = new ArrayList<>();
        for (Vector3ic vector3ic : iterable) {
            actual.add(new Vector3i(vector3ic));
        }

        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new HashSet<>(expectedPositions(region)), new HashSet<>(actual));
    }

    @Test
    public void testPlaneOfBlocksRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 1, 1));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        List<Vector3ic> actual = new ArrayList<>();
        for (Vector3ic vector3ic : iterable) {
            actual.add(new Vector3i(vector3ic));
        }

        Assertions.assertEquals(4, actual.size());
        Assertions.assertEquals(new HashSet<>(expectedPositions(region)), new HashSet<>(actual));
    }

    @Test
    public void testBoxOfBlocksRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(1, 1, 1));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        List<Vector3ic> actual = new ArrayList<>();
        for (Vector3ic vector3ic : iterable) {
            actual.add(new Vector3i(vector3ic));
        }

        Assertions.assertEquals(8, actual.size());
        Assertions.assertEquals(new HashSet<>(expectedPositions(region)), new HashSet<>(actual));
    }

    private List<Vector3ic> expectedPositions(BlockRegion region) {
        List<Vector3ic> result = new ArrayList<>(region.getSizeX() * region.getSizeY() * region.getSizeZ());
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    result.add(new Vector3i(x, y, z));
                }
            }
        }
        return result;
    }
}
