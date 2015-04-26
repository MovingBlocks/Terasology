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

package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.SpiralIterable;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class ProximitySpawner implements Spawner {

    @Override
    public Vector3f getSpawnPosition(World world, EntityRef entity) {
        // TODO: use location component instead

        int centerX = ChunkConstants.SIZE_X / 2;
        int centerY = ChunkConstants.SIZE_Y / 2;
        int centerZ = ChunkConstants.SIZE_Z / 2;
        Vector3i spawnPos = new Vector3i(centerX, centerY, centerZ);
        return getSpawnPosition(world, spawnPos);
    }

    private Vector3f getSpawnPosition(World world, Vector3i spawnPos) {

        int rad = 16;
        // try and find somewhere in this chunk a spot to land
        Vector3i ext = new Vector3i(rad, 8, rad);
        Region3i spawnArea = Region3i.createFromCenterExtents(spawnPos, ext);
        Region worldRegion = world.getWorldData(spawnArea);
        Vector2i spawnPos2d = new Vector2i(spawnPos.getX(), spawnPos.getZ());

        // check if generation uses sea level and surface height facets
        SurfaceHeightFacet surfaceHeightFacet = worldRegion.getFacet(SurfaceHeightFacet.class);
        SeaLevelFacet seaLevelFacet = worldRegion.getFacet(SeaLevelFacet.class);
        int seaLevel = (seaLevelFacet != null) ? seaLevelFacet.getSeaLevel() : 0;

        if (surfaceHeightFacet != null) {
            for (BaseVector2i pos : SpiralIterable.clockwise(spawnPos2d, rad)) {
                float val = surfaceHeightFacet.get(pos.getX(), pos.getY());
                int height = TeraMath.floorToInt(val);
                if (height > seaLevel) {
                    return new Vector3f(pos.getX(), height, pos.getY());
                }
            }
        }

        return spawnPos.toVector3f();
    }

}
