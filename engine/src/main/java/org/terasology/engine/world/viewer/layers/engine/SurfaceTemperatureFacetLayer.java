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

package org.terasology.world.viewer.layers.engine;

import org.terasology.world.generation.facets.SurfaceTemperatureFacet;
import org.terasology.world.viewer.layers.FieldFacetLayer;
import org.terasology.world.viewer.layers.Renders;
import org.terasology.world.viewer.layers.ZOrder;

/**
 * Provides information about the surface temperature.
 */
@Renders(value = SurfaceTemperatureFacet.class, order = ZOrder.SURFACE)
public class SurfaceTemperatureFacetLayer extends FieldFacetLayer {

    public SurfaceTemperatureFacetLayer(Config config) {
        super(SurfaceTemperatureFacet.class, config);
    }

    public SurfaceTemperatureFacetLayer() {
        super(SurfaceTemperatureFacet.class, 0d, 100d);
        setVisible(false);
    }
}
