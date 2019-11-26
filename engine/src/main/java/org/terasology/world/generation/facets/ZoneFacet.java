/*
 * Copyright 2019 MovingBlocks
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

import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetName;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;

/**
 * A facet that allows large regions of the world to be grouped together.
 * It is intended to be multiplied and used as a modulus for worldgen that could have multiple values,
 * so that it may remain consistent across wide areas. For example, biomes.
 * Not related to @org.terasology.world.zones.Zone
 */
@FacetName("Zone")
public class ZoneFacet extends BaseFieldFacet2D {
    static int maxSamplesPerRegion = 5;

    public ZoneFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
