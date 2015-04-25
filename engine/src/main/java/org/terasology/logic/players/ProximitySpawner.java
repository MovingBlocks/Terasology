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
        int centerX = ChunkConstants.SIZE_X / 2;
        int centerY = ChunkConstants.SIZE_Y / 2;
        int centerZ = ChunkConstants.SIZE_Z / 2;
        Vector3f spawnPos = new Vector3f(centerX, centerY, centerZ);
        return getSpawnPosition(world, spawnPos);
    }

    public Vector3f getSpawnPosition(World world, Vector3f spawnPos) {

        // try and find somewhere in this chunk a spot to land
        Region3i spawnArea = Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), ChunkConstants.CHUNK_SIZE);
        Region worldRegion = world.getWorldData(spawnArea);

        //check if generation uses sea level and surfaceheight facets
        SurfaceHeightFacet surfaceHeightFacet = worldRegion.getFacet(SurfaceHeightFacet.class);
        SeaLevelFacet seaLevelFacet = worldRegion.getFacet(SeaLevelFacet.class);
        if (surfaceHeightFacet != null && seaLevelFacet != null) {
            int seaLevel = seaLevelFacet.getSeaLevel();

            for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
                int height = TeraMath.floorToInt(surfaceHeightFacet.get(pos.x, pos.z));
                if (height > seaLevel) {
                    pos.y = height;
                    if (findOpenVerticalPosition(pos)) {
                        return pos.toVector3f();
                    }
                }
            }
        }
        Vector3i pos = new Vector3i(spawnPos.x, spawnPos.y, spawnPos.z);
        if (findOpenVerticalPosition(pos)) {
            return pos.toVector3f();
        }

        return spawnPos;
    }

    private boolean findOpenVerticalPosition(Vector3i spawnPos) {
        // find a spot above the surface that is big enough for this character
        int consecutiveAirBlocks = 0;
        for (int i = 1; i < 20; i++) {
            spawnPos.add(0, 1, 0);
//            if (worldProvider.getBlock(spawnPos) == BlockManager.getAir()) {
            if (true) {
                consecutiveAirBlocks++;
            } else {
                consecutiveAirBlocks = 0;
            }

            if (consecutiveAirBlocks >= 2) {
                spawnPos.add(0, 1 - consecutiveAirBlocks, 0);
                return true;
            }
        }

        return false;
    }
}
