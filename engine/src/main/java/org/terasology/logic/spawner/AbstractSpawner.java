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

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.math.Region3i;
import org.terasology.math.SpiralIterable;
import org.terasology.math.TeraMath;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SpawnHeightFacet;
import org.terasology.world.generation.facets.StrictlySparseSeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import java.util.Optional;
import java.util.function.Function;

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
        Vector3i desiredPos = new Vector3i(pos.x(), 1, pos.y());

        // try and find somewhere in this region a spot to land
        Region3i spawnArea = Region3i.createFromCenterExtents(desiredPos, ext);
        Region worldRegion = world.getWorldData(spawnArea);

        Function<Vector2i, Optional<Float>> getWorld;

        // check if generation uses sea level and surface height facets
        SurfaceHeightFacet surfaceHeightFacet = worldRegion.getFacet(SurfaceHeightFacet.class);
        SpawnHeightFacet spawnHeightFacet = worldRegion.getFacet(SpawnHeightFacet.class);

        if (spawnHeightFacet != null) {
            getWorld = v -> spawnHeightFacet.getWorld(v.x(), v.y());
        }
        else if (surfaceHeightFacet != null) {
            getWorld = v -> Optional.of(surfaceHeightFacet.getWorld(v.x(), v.y()));
        }
        else {
            throw new IllegalStateException("Surface height facet and spawn height facet not found");
        }

        Function<Vector2i, Optional<Integer>> getSeaLevel;

        SeaLevelFacet seaLevelFacet = worldRegion.getFacet(SeaLevelFacet.class);
        StrictlySparseSeaLevelFacet sparseSeaLevelFacet = worldRegion.getFacet(StrictlySparseSeaLevelFacet.class);

        if (sparseSeaLevelFacet != null) {
            getSeaLevel = v -> sparseSeaLevelFacet.getSeaLevel(v.x(), v.y());
        }
        else if (seaLevelFacet != null) {
            getSeaLevel = v -> Optional.of(seaLevelFacet.getSeaLevel());
        }
        else {
            getSeaLevel = v -> Optional.of(0);
        }

        int spiralRad = searchRadius / 2 - 1;
        SpiralIterable spiral = SpiralIterable.clockwise(pos).maxRadius(spiralRad).scale(2).build();
        for (Vector2ic test : spiral) {
            Optional<Float> val = getWorld.apply(new Vector2i(test));
            if(!val.isPresent()) {
                continue;
            }
            int height = TeraMath.floorToInt(val.get());
            if (!getSeaLevel.apply(new Vector2i(test)).isPresent() || height >= getSeaLevel.apply(new Vector2i(test)).get()) {
                return new Vector3f(test.x(), height, test.y());
            }
        }

        // nothing above sea level found
        for (Vector2ic test : spiral) {
            Optional<Float> val = getWorld.apply(new Vector2i(test));
            if(!val.isPresent()) {
                continue;
            }
            return new Vector3f(test.x(), TeraMath.floorToInt(val.get()), test.y());
        }

        throw new IllegalStateException("No spawn location found");
    }
}
