// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.spawner;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SpawnHeightFacet;
import org.terasology.engine.world.generation.facets.StrictlySparseSeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.SpiralIterable;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractSpawner implements Spawner {

    /**
     * Tries to find a suitable spawning point based on {@link SurfaceHeightFacet} and {@link SeaLevelFacet}.
     *
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

        Function<BaseVector2i, Optional<Float>> getWorld;

        // check if generation uses sea level and surface height facets
        SurfaceHeightFacet surfaceHeightFacet = worldRegion.getFacet(SurfaceHeightFacet.class);
        SpawnHeightFacet spawnHeightFacet = worldRegion.getFacet(SpawnHeightFacet.class);

        if (spawnHeightFacet != null) {
            getWorld = v -> spawnHeightFacet.getWorld(v.getX(), v.getY());
        } else if (surfaceHeightFacet != null) {
            getWorld = v -> Optional.of(surfaceHeightFacet.getWorld(v.getX(), v.getY()));
        } else {
            throw new IllegalStateException("Surface height facet and spawn height facet not found");
        }

        Function<BaseVector2i, Optional<Integer>> getSeaLevel;

        SeaLevelFacet seaLevelFacet = worldRegion.getFacet(SeaLevelFacet.class);
        StrictlySparseSeaLevelFacet sparseSeaLevelFacet = worldRegion.getFacet(StrictlySparseSeaLevelFacet.class);

        if (sparseSeaLevelFacet != null) {
            getSeaLevel = v -> sparseSeaLevelFacet.getSeaLevel(v.getX(), v.getY());
        } else if (seaLevelFacet != null) {
            getSeaLevel = v -> Optional.of(seaLevelFacet.getSeaLevel());
        } else {
            getSeaLevel = v -> Optional.of(0);
        }

        int spiralRad = searchRadius / 2 - 1;
        SpiralIterable spiral = SpiralIterable.clockwise(pos).maxRadius(spiralRad).scale(2).build();
        for (BaseVector2i test : spiral) {
            Optional<Float> val = getWorld.apply(test);
            if (!val.isPresent()) {
                continue;
            }
            int height = TeraMath.floorToInt(val.get());
            if (!getSeaLevel.apply(test).isPresent() || height >= getSeaLevel.apply(test).get()) {
                return new Vector3f(test.getX(), height, test.getY());
            }
        }

        // nothing above sea level found
        for (BaseVector2i test : spiral) {
            Optional<Float> val = getWorld.apply(test);
            if (!val.isPresent()) {
                continue;
            }
            return new Vector3f(test.getX(), TeraMath.floorToInt(val.get()), test.getY());
        }

        throw new IllegalStateException("No spawn location found");
    }
}
