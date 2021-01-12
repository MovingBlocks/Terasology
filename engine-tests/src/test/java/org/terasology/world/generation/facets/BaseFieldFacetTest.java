/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFieldFacet3D;
import org.terasology.world.generation.facets.base.FieldFacet3D;

/**
 * Tests the {@link BaseFieldFacet3D} class.
 */
public class BaseFieldFacetTest extends FieldFacetTest {

    @Override
    protected FieldFacet3D createFacet(BlockRegion region, Border3D border) {
        return new BaseFieldFacet3D(region, border) {
            // this class is abstract, but we don't want specific implementations
        };
    }


}
