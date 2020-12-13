// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Rectanglei;
import org.joml.Vector2ic;

public final class BlockAreas {
    private BlockAreas() {
    }

    public static BlockArea fromMinAndMax(Vector2ic min, Vector2ic max) {
        return new BlockArea().setMin(min).setMax(max);
    }

    public static BlockArea fromMinAndMax(int minX, int minY, int maxX, int maxY) {
        return new BlockArea().setMin(minX, minY).setMax(maxX, maxY);
    }

    public static BlockArea fromRectangle(Rectanglei rectangle) {
        return new BlockArea(rectangle);
    }
}
