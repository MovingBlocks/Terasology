// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.layers.engine;

import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.engine.world.viewer.layers.FieldFacetLayer;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;

/**
 * Provides information about the surface humidity.
 */
@Renders(value = SurfaceHumidityFacet.class, order = ZOrder.SURFACE)
public class SurfaceHumidityFacetLayer extends FieldFacetLayer {

    public SurfaceHumidityFacetLayer(Config config) {
        super(SurfaceHumidityFacet.class, config);
    }

    public SurfaceHumidityFacetLayer() {
        super(SurfaceHumidityFacet.class, 0d, 100d);
        setVisible(false);
    }
}
