// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.spawner;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.math.SpiralIterable;
import org.terasology.math.TeraMath;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SpawnHeightFacet;
import org.terasology.engine.world.generation.facets.StrictlySparseSeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractSpawner implements Spawner {

    /**
     * Tries to find a suitable spawning point based on {@link SurfacesFacet} and {@link ElevationFacet}.
     * @param searchRadius the radius within a suitable spawning point will be searched
     * @param world the facet-based world
     * @param pos the desired 2D position in that world
     * @return a 3D position above the surface and sea level or <code>null</code> if none was found
     * @throws IllegalStateException if no required facets can be created.
     */
    protected Vector3f findSpawnPosition(World world, Vector2i pos, int searchRadius) {

        Vector3i ext = new Vector3i(searchRadius, searchRadius, searchRadius);
        Vector3i desiredPos = new Vector3i(pos.x(), getStartHeight(world, pos), pos.y());

        // try and find somewhere in this region a spot to land
        BlockRegion spawnArea = new BlockRegion(desiredPos).expand(ext);
        Region worldRegion = world.getWorldData(spawnArea);

        Function<Vector2ic, Optional<Float>> getWorld;

        // check if generation uses sea level and surface height facets
        SurfacesFacet surfacesFacet = worldRegion.getFacet(SurfacesFacet.class);
        ElevationFacet elevationFacet = worldRegion.getFacet(ElevationFacet.class);
        SpawnHeightFacet spawnHeightFacet = worldRegion.getFacet(SpawnHeightFacet.class);

        if (spawnHeightFacet != null) {
            getWorld = v -> spawnHeightFacet.getWorld(v.x(), v.y());
        } else if (elevationFacet != null) {
            if (surfacesFacet != null) {
                getWorld = v -> surfacesFacet.getPrimarySurface(elevationFacet, v.x(), v.y());
            } else {
                getWorld = v -> Optional.of(elevationFacet.getWorld(v.x(), v.y()));
            }
        } else {
            throw new IllegalStateException("No spawn height facet or elevation facet facet found. Can't place spawn point.");
        }

        Function<Vector2ic, Optional<Integer>> getSeaLevel;

        SeaLevelFacet seaLevelFacet = worldRegion.getFacet(SeaLevelFacet.class);
        StrictlySparseSeaLevelFacet sparseSeaLevelFacet = worldRegion.getFacet(StrictlySparseSeaLevelFacet.class);

        if (sparseSeaLevelFacet != null) {
            getSeaLevel = v -> sparseSeaLevelFacet.getSeaLevel(v.x(), v.y());
        } else if (seaLevelFacet != null) {
            getSeaLevel = v -> Optional.of(seaLevelFacet.getSeaLevel());
        } else {
            getSeaLevel = v -> Optional.of(0);
        }

        int spiralRad = searchRadius / 2 - 1;
        SpiralIterable spiral = SpiralIterable.clockwise(pos).maxRadius(spiralRad).scale(2).build();
        for (Vector2ic test : spiral) {
            Optional<Float> val = getWorld.apply(test);
            if (!val.isPresent()) {
                continue;
            }
            int height = TeraMath.floorToInt(val.get());
            if (!getSeaLevel.apply(test).isPresent() || height >= getSeaLevel.apply(test).get()) {
                return new Vector3f(test.x(), height, test.y());
            }
        }

        // nothing above sea level found
        for (Vector2ic test : spiral) {
            Optional<Float> val = getWorld.apply(test);
            if (val.isPresent()) {
                return new Vector3f(test.x(), TeraMath.floorToInt(val.get()), test.y());
            }
        }

        throw new IllegalStateException("No spawn location found");
    }

    /**
     * Get the elevation at a single point, to use as the base point for searching.
     */
    private int getStartHeight(World world, Vector2i pos) {
        BlockRegion spawnArea = new BlockRegion(pos.x(), 0, pos.y());
        Region worldRegion = world.getWorldData(spawnArea);

        ElevationFacet elevationFacet = worldRegion.getFacet(ElevationFacet.class);

        if (elevationFacet != null) {
            return (int) elevationFacet.getWorld(pos);
        } else {
            // We'll have to rely on the SurfaceHeightFacet or SpawnHeightFacet anyway, and those are purely 2D so the height doesn't matter.
            return 0;
        }
    }
}
