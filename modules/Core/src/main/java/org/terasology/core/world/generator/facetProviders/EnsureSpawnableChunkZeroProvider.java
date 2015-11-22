/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.core.world.generator.facetProviders;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * makes sure that chunk zero has a piece of land above the water
 */
@Updates(@Facet(SurfaceHeightFacet.class))
@Requires(@Facet(SeaLevelFacet.class))
public class EnsureSpawnableChunkZeroProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        // will give funny results for regions that are not exactly chunk sized, but it is better than sinking in the water on spawn
        Vector3i centerChunkPos = new Vector3i(ChunkConstants.CHUNK_REGION.center());
        if (region.getRegion().encompasses(centerChunkPos)) {
            SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
            SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
            float seaLevel = seaLevelFacet.getSeaLevel();
            float targetHeight = seaLevel + 1; // one block above the seaLevel

            // update the surface height so that it spikes up to sea level
            Vector2i middlePos = new Vector2i(centerChunkPos.x, centerChunkPos.z);
            for (BaseVector2i pos : facet.getWorldRegion().contents()) {
                float originalValue = facet.getWorld(pos);
                if (seaLevel > originalValue) {
                    // the surface is below sea level
                    float scaleTowardsSeaLevel = (float) pos.gridDistance(middlePos) / (float) (ChunkConstants.SIZE_X / 2);
                    if (scaleTowardsSeaLevel < 1f) {
                        facet.setWorld(pos, TeraMath.lerp(originalValue, targetHeight, 1f - scaleTowardsSeaLevel));
                    }
                }
            }
        }
    }
}
