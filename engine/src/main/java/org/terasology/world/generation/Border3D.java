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
import org.terasology.math.geom.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;

import java.util.Objects;

/**
 */
public class Border3D {
    private final int top;
    private final int bottom;
    private final int sides;

    public Border3D(int top, int bottom, int sides) {
        Preconditions.checkArgument(top >= 0);
        Preconditions.checkArgument(bottom >= 0);
        Preconditions.checkArgument(sides >= 0);
        this.top = top;
        this.bottom = bottom;
        this.sides = sides;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getSides() {
        return sides;
    }

    public Rect2i expandTo2D(Region3i region) {
        return Rect2i.createFromMinAndMax(region.minX() - getSides(), region.minZ() - getSides(),
                region.maxX() + getSides(), region.maxZ() + getSides());
    }

    public Rect2i expandTo2D(Vector3i size) {
        return Rect2i.createFromMinAndMax(-getSides(), -getSides(), size.x + getSides() - 1, size.z + getSides() - 1);
    }

    public Region3i expandTo3D(Region3i region) {
        return Region3i.createFromMinMax(new Vector3i(region.minX() - sides, region.minY() - bottom, region.minZ() - sides),
                new Vector3i(region.maxX() + sides, region.maxY() + top, region.maxZ() + sides));
    }

    public Region3i expandTo3D(Vector3i size) {
        return Region3i.createFromMinMax(new Vector3i(-sides, -bottom, -sides),
                new Vector3i(size.x + sides - 1, size.y + top - 1, size.z + sides - 1));
    }

    public Border3D extendBy(int topExtension, int bottomExtension, int sidesExtension) {
        return new Border3D(top + topExtension, bottom + bottomExtension, sides + sidesExtension);
    }

    public Border3D maxWith(int topValue, int bottomValue, int sidesValue) {
        return new Border3D(
                Math.max(top, topValue),
                Math.max(bottom, bottomValue),
                Math.max(sides, sidesValue));
    }

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
