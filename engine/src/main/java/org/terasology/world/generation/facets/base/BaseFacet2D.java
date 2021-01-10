/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.generation.facets.base;

import org.joml.Vector3i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.WorldFacet2D;

/**
 */
public class BaseFacet2D implements WorldFacet2D {

    private BlockArea worldRegion;
    private BlockArea relativeRegion;

    public BaseFacet2D(BlockRegionc targetRegion, Border3D border) {
        worldRegion = border.expandTo2D(targetRegion);
        relativeRegion = border.expandTo2D(targetRegion.getSize(new Vector3i()));
    }

    @Override
    public final BlockAreac getWorldArea() {
        return worldRegion;
    }

    @Override
    public final BlockAreac getRelativeArea() {
        return relativeRegion;
    }

    protected final int getRelativeIndex(int x, int z) {
        if (!relativeRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, relativeRegion.toString()));
        }
        return x - relativeRegion.minX() + relativeRegion.getSizeX() * (z - relativeRegion.minY());
    }

    protected final int getWorldIndex(int x, int z) {
        if (!worldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, worldRegion.toString()));
        }
        return x - worldRegion.minX() + worldRegion.getSizeX() * (z - worldRegion.minY());
    }
}
