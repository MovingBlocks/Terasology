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
package org.terasology.world.generation;

import com.google.common.base.Preconditions;
import org.joml.Vector3ic;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;

import java.util.Objects;

/**
 * Borders extend the facet's data range. This is necessary for most Facets which may cross Chunk borders.
 */
public class Border3D {
    private final int top;
    private final int bottom;
    private final int sides;

    /**
     * Constructor defining the sizes of the border.
     * @param top The extra space at the top of chunk.
     * @param bottom The extra space at the bottom of chunk.
     * @param sides The extra space at the 4 sides of the chunk. These sides are North, East, South, West.
     */
    public Border3D(int top, int bottom, int sides) {
        Preconditions.checkArgument(top >= 0);
        Preconditions.checkArgument(bottom >= 0);
        Preconditions.checkArgument(sides >= 0);
        this.top = top;
        this.bottom = bottom;
        this.sides = sides;
    }

    public Border3D(FacetBorder border) {
        this(border.top(), border.bottom(), border.sides());
    }

    /**@return Returns the extra space at the top.    */
    public int getTop() {
        return top;
    }

    /**@return Returns the extra space at the bottom.    */
    public int getBottom() {
        return bottom;
    }

    /**@return Returns the extra space at the 4 sides.    */
    public int getSides() {
        return sides;
    }

    /**
     * Returns a 2D representation of the world (top view) with the given borders added to the size of the original region.
     * @param region The original region to be used.
     * @return The 2D representation with the additional space added to it.
     */
    public BlockArea expandTo2D(BlockRegionc region) {
        return new BlockArea(region.minX() - getSides(), region.minZ() - getSides(),
                region.maxX() + getSides(), region.maxZ() + getSides());
    }

    /**
     * Same as {@code {@link #expandTo2D(BlockRegionc)}} but with a Vector3i instead of a Region3i.
     * @param size The size used.
     * @return The 2D representation with the additional space added to it with the additional space added to it in the 3 dimensions.
     */
    //TODO: offer a variant that takes three integers to potentially avoid allocation of superfluous vectors
    public BlockArea expandTo2D(Vector3ic size) {
        return new BlockArea(-getSides(), -getSides(), size.x() + getSides() - 1, size.z() + getSides() - 1);
    }

    /**
     * Returns a 3D representation of the workd, a region in this case. With the borders added to it.
     * @param region The region to be expanded with the borders.
     * @return The 3D world representation with the additional space added to it in the 3 dimensions.
     */
    public BlockRegion expandTo3D(BlockRegionc region) {
        return new BlockRegion(region.minX() - sides, region.minY() - bottom, region.minZ() - sides,
                region.maxX() + sides, region.maxY() + top, region.maxZ() + sides);
    }

    /**
     * Same as {@code {@link #expandTo3D(BlockRegionc)}}} but with a Vector3i instead of a Region3i.
     * @param size The size to be used.
     * @return The 3D world representation with the additional space added to it in the 3 dimensions.
     */
    //TODO: offer a variant that takes three integers to potentially avoid allocation of superfluous vectors
    public BlockRegion expandTo3D(Vector3ic size) {
        return new BlockRegion(-sides, -bottom, -sides,
                size.x() + sides - 1, size.y() + top - 1, size.z() + sides - 1);
    }

    /**
     * Extends the border by using the sizes of another border.
     * @param topExtension The top extension to be added.
     * @param bottomExtension The bottom extension to be added.
     * @param sidesExtension The side extensions to be added.
     * @return The border with the extra extensions.
     */
    public Border3D extendBy(int topExtension, int bottomExtension, int sidesExtension) {
        return new Border3D(top + topExtension, bottom + bottomExtension, sides + sidesExtension);
    }

    /**
     * Extends the border by using the sizes of another border.
     * @param other The border to add to this one
     * @return The new border with the extra extensions.
     */
    public Border3D extendBy(Border3D other) {
        return new Border3D(top + other.top, bottom + other.bottom, sides + other.sides);
    }

    /**
     * Returns a new border, using the largest value of each extension for both borders. Border A(Sides=5,Bottom=4,Top=3) maxed with border B (Sides=3,Bottom=4,Top=5)
     * would make a border C with boundries: (5,4,5).
     * @param other The top value to compare with the instance's top value.
     * @return The resulting new Border3D.
     */
    public Border3D maxWith(Border3D other) {
        if (other == null) {
            return new Border3D(top, bottom, sides);
        } else {
            return new Border3D(
                    Math.max(top, other.top),
                    Math.max(bottom, other.bottom),
                    Math.max(sides, other.sides));
        }
    }

    /**
     * Checks if the fields of the instance are the same as the object passed in the parameters. It compares the size of the Border3D and returns true of all sizes are the
     * same.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Border3D) {
            Border3D other = (Border3D) obj;
            return other.top == top && other.bottom == bottom && other.sides == sides;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, bottom, sides);
    }
}
