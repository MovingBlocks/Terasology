/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseStrictlySparseFieldFacet2D;

/***
 * Stores the height at which the player may be spawned, if it exists for a given coordinate
 */
public class SpawnHeightFacet extends BaseStrictlySparseFieldFacet2D {
    public SpawnHeightFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
