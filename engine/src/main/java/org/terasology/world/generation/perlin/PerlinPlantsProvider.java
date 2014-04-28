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
package org.terasology.world.generation.perlin;

import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.BiomeFacet;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.PlantFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * @author Immortius
 */
@Requires({SurfaceHeightFacet.class, BiomeFacet.class, DensityFacet.class}*)
@Produces(PlantFacet.class)
public class PerlinPlantsProvider implements FacetProvider {
    private SimplexNoise noise;

    @Override
    public void setSeed(long seed) {
        noise = new SimplexNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        PlantFacet result = new PlantFacet(region.getRegion().size());
        result.get();
    }
}
