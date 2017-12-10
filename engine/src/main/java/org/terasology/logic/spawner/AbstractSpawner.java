/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.spawner;

import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.SpiralIterable;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SpawnHeightFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.OptionalFacet2D;

import java.util.Optional;

public abstract class AbstractSpawner implements Spawner {

    /**
     * Tries to find a suitable spawning point based on {@link SurfaceHeightFacet} and {@link SeaLevelFacet}.
     * @param searchRadius the radius within a suitable spawning point will be searched
     * @param world the facet-based world
     * @param pos the desired 2D position in that world
     * @return a 3D position above the surface and sea level or <code>null</code> if none was found
     * @throws IllegalStateException if no SurfaceHeightFacet can be created.
     */
    protected Vector3f findSpawnPosition(World world, Vector2i pos, int searchRadius) {

        Vector3i ext = new Vector3i(searchRadius, 1, searchRadius);
        Vector3i desiredPos = new Vector3i(pos.getX(), 1, pos.getY());

        // try and find somewhere in this region a spot to land
        Region3i spawnArea = Region3i.createFromCenterExtents(desiredPos, ext);
        Region worldRegion = world.getWorldData(spawnArea);

        // check if generation uses sea level and surface height facets
        OptionalFacet2D spawnHeightFacet = worldRegion.getFacet(SurfaceHeightFacet.class);
        if (spawnHeightFacet == null) {
            spawnHeightFacet = worldRegion.getFacet(SpawnHeightFacet.class);
            if (spawnHeightFacet == null) {
                throw new IllegalStateException("Surface or spawn height facet not found");
            }
        }

        SeaLevelFacet seaLevelFacet = worldRegion.getFacet(SeaLevelFacet.class);
        int seaLevel = (seaLevelFacet != null) ? seaLevelFacet.getSeaLevel() : 0;

        int spiralRad = searchRadius / 2 - 1;
        SpiralIterable spiral = SpiralIterable.clockwise(pos).maxRadius(spiralRad).scale(2).build();
        for (BaseVector2i test : spiral) {
            Optional<Float> opt = spawnHeightFacet.getOptionalWorld(test.getX(), test.getY());
            if (!opt.isPresent()) {
                continue;
            }
            float val = opt.get();
            int height = TeraMath.floorToInt(val);
            if (height >= seaLevel) {
                return new Vector3f(test.getX(), height, test.getY());
            }
        }

        // nothing above sea level found
        for (BaseVector2i test : spiral) {
            Optional<Float> opt = spawnHeightFacet.getOptionalWorld(test.getX(), test.getY());
            if (!opt.isPresent()) {
                continue;
            }
            float val = opt.get();
            return new Vector3f(test.getX(), TeraMath.floorToInt(val), test.getY());
        }

        throw new IllegalStateException("No valid spawn locations");
    }
}
