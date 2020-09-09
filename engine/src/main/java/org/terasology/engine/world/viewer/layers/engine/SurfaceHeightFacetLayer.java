// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.layers.engine;

import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.engine.world.viewer.layers.FieldFacetLayer;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;

/**
 * Provides information about the surface height level.
 */
@Renders(value = SurfaceHeightFacet.class, order = ZOrder.SURFACE)
public class SurfaceHeightFacetLayer extends FieldFacetLayer {

    /**
     * This is called through reflection.
     *
     * @param config the configuration params
     */
    public SurfaceHeightFacetLayer(Config config) {
        super(SurfaceHeightFacet.class, config);
    }

    public SurfaceHeightFacetLayer() {
        super(SurfaceHeightFacet.class, 0d, 1.0d);
    }
}
