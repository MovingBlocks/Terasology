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

import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseBooleanFieldFacet3D;
import org.terasology.world.generation.facets.base.BooleanFieldFacet3D;

/**
 * Tests the {@link BaseBooleanFieldFacet3D} class.
 */
public class BaseBooleanFacetTest extends BooleanFacetTest {

    @Override
    protected BooleanFieldFacet3D createFacet(Region3i region, Border3D border) {
        return new BaseBooleanFieldFacet3D(region, border) {
            // this class is abstract, but we don't want specific implementations
        };
    }


}
