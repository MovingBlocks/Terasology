// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.engine.world.block.Blocks;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlocksTest {

    static Stream<Arguments> coordinates() {
        return Stream.of(
                Arguments.of(new Vector3f(.2f, .2f, .2f), new Vector3i(0, 0, 0)),
                Arguments.of(new Vector3f(.49f, .49f, .49f), new Vector3i(0, 0, 0)),
                Arguments.of(new Vector3f(.5f, .5f, .5f), new Vector3i(1, 1, 1)),
                Arguments.of(new Vector3f(1, 1, 1), new Vector3i(1, 1, 1)),
                Arguments.of(new Vector3f(-.1f, -.1f, -.1f), new Vector3i(0, 0, 0)),
                Arguments.of(new Vector3f(-.5f, -.5f, -.5f), new Vector3i(0, 0, 0)),
                Arguments.of(new Vector3f(-.51f, -.51f, -.51f), new Vector3i(-1, -1, -1))
        );
    }

    @ParameterizedTest
    @MethodSource("coordinates")
    @DisplayName("toBlockPos should round half-up towards positive infinity")
    public void testToBlockPos(Vector3f worldPos, Vector3i expectedBlockPos) {
        assertEquals(expectedBlockPos, Blocks.toBlockPos(worldPos));
    }
}
