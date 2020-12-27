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
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.WorldFacet3D;

/**
 */
public class BaseFacet3D implements WorldFacet3D {

    private BlockRegion worldRegion;
    private BlockRegion relativeRegion;

    public BaseFacet3D(BlockRegionc targetRegion, Border3D border) {
        worldRegion = border.expandTo3D(targetRegion);
        relativeRegion = border.expandTo3D(targetRegion.getSize(new Vector3i()));
    }

    @Override
    public final BlockRegion getWorldRegion() {
        return worldRegion;
    }

    @Override
    public final BlockRegion getRelativeRegion() {
        return relativeRegion;
    }

    protected final int getRelativeIndex(int x, int y, int z) {
        if (!relativeRegion.contains(x, y, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d, %d) for region %s", x, y, z, relativeRegion.toString()));
        }
        return x - relativeRegion.minX() + relativeRegion.getSizeX() * (y - relativeRegion.minY() + relativeRegion.getSizeY() * (z - relativeRegion.minZ()));
    }

    protected final int getWorldIndex(int x, int y, int z) {
        if (!worldRegion.contains(x, y, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d, %d) for region %s", x, y, z, worldRegion.toString()));
        }
        return x - worldRegion.minX() + worldRegion.getSizeX() * (y - worldRegion.minY() + worldRegion.getSizeY() * (z - worldRegion.minZ()));
    }
}
