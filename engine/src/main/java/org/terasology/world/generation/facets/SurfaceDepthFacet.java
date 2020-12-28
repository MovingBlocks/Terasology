/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.generation.facets;

import org.terasology.math.Region3i;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;

/**
 * Stores the surface depth limits.
 * The surface depth limit (inclusive) is an optional bottom limit for the default world generator.
 * No blocks below the surface depth limit are altered..
 */
public class SurfaceDepthFacet extends BaseFieldFacet2D {

    public SurfaceDepthFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
