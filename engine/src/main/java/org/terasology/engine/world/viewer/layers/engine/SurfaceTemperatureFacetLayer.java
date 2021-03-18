// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.layers.engine;

import org.terasology.engine.world.generation.facets.SurfaceTemperatureFacet;
import org.terasology.engine.world.viewer.layers.FieldFacetLayer;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;

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
