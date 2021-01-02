/*
 * Copyright 2018 MovingBlocks
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

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.WorldFacet2D;

/***
 * A base class for sparse (map-based) 2D facets.
 */
public abstract class BaseSparseFacet2D implements WorldFacet2D {
    private BlockAreac worldArea;
    private BlockAreac relativeArea;

    public BaseSparseFacet2D(BlockRegion targetRegion, Border3D border) {
        worldArea = border.expandTo2D(targetRegion);
        relativeArea = border.expandTo2D(targetRegion.getSize(new Vector3i()));
    }

    @Override
    public BlockAreac getWorldArea() {
        return worldArea;
    }

    @Override
    public BlockAreac getRelativeArea() {
        return relativeArea;
    }

    protected Vector2ic worldToRelative(int x, int y) {
        return new Vector2i(x - getWorldArea().minX() + getRelativeArea().minX(),
                y - getWorldArea().minY() + getRelativeArea().minY());
    }

    protected void validateCoord(int x, int y, BlockAreac area) {
        if(!area.contains(x, y)) {
            String text = "Out of bounds: (%d, %d) for region %s";
            String msg = String.format(text, x, y, area.toString());
            throw new IllegalArgumentException(msg);
        }
    }
}
