// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import com.google.common.collect.ImmutableList;
import org.joml.Vector3i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockRegionIterableTest {

    @Test
    public void testSingleBlockRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        Assertions.assertEquals(1, ImmutableList.copyOf(iterable).size());
    }

    @Test
    public void testLineOfBlocksRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 1, 0));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        Assertions.assertEquals(2, ImmutableList.copyOf(iterable).size());
    }

    @Test
    public void testPlaneOfBlocksRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 1, 1));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        Assertions.assertEquals(4, ImmutableList.copyOf(iterable).size());
    }

    @Test
    public void testBoxOfBlocksRegion() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(1, 1, 1));
        BlockRegionIterable iterable = BlockRegionIterable.region(region).build();

        Assertions.assertEquals(8, ImmutableList.copyOf(iterable).size());
    }
}
