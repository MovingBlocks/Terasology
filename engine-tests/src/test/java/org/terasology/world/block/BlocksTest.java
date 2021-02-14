// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlocksTest {

    @Test
    public void testToBlockPos() {
        assertEquals(Blocks.toBlockPos(new Vector3f(.2f, .2f, .2f), new Vector3i()), new Vector3i(0, 0, 0));
        assertEquals(Blocks.toBlockPos(new Vector3f(-.2f, -.2f, -.2f), new Vector3i()), new Vector3i(0, 0, 0));
        assertEquals(Blocks.toBlockPos(new Vector3f(.56f, 0, 0), new Vector3i()), new Vector3i(1, 0, 0));
    }
}
