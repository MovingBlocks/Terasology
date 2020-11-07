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
package org.terasology.world.generation.facets;

import org.joml.Vector3ic;
import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.VerticallySparseBooleanFacet3D;

import java.util.Optional;

/**
 * This facet represents the heights of all of the surfaces of the ground. There may be multiple surfaces
 * in one column, for example, where there are overhangs, or floating islands. This facet is intended to
 * be used for placement of features on the surface itself, such as grass, trees and buildings.
 *
 * The combination of the SurfacesFacet and the ElevationFacet is a more flexible alternative to the SurfaceHeightFacet.
 */
public class SurfacesFacet extends VerticallySparseBooleanFacet3D {

    public SurfacesFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    /**
     * Get the height of the surface closest to ground level.
     */
    public Optional<Float> getPrimarySurface(ElevationFacet elevationFacet, int x, int z) {
        float elevation = elevationFacet.getWorld(x, z);
        float bestDistance = Float.POSITIVE_INFINITY;
        Optional<Float> bestSurface = Optional.empty();
        for (int surface : getWorldColumn(x, z)) {
            float distance = Math.abs(surface - elevation);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestSurface = Optional.of((float) surface);
            }
        }
        return bestSurface;
    }

    public int getNextAbove(Vector3ic pos) {
        int best = Integer.MAX_VALUE / 4; // Don't actually use the maximum value, to reduce the risk of overflow.
        for (int surface : getWorldColumn(pos.x(), pos.z())) {
            if (surface >= pos.y() && surface < best) {
                best = surface;
            }
        }
        return best;
    }

    public int getNextBelow(Vector3ic pos) {
        int best = Integer.MIN_VALUE / 4; // Don't actually use the minimum value, to reduce the risk of overflow.
        for (int surface : getWorldColumn(pos.x(), pos.z())) {
            if (surface <= pos.y() && surface > best) {
                best = surface;
            }
        }
        return best;
    }
}
