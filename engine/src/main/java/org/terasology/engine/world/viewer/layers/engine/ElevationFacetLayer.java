// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.layers.engine;

import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.viewer.layers.FieldFacetLayer;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;

/**
 * Provides information about the surface height level.
 */
@Renders(value = ElevationFacet.class, order = ZOrder.SURFACE)
public class ElevationFacetLayer extends FieldFacetLayer {

    /**
     * This is called through reflection.
     * @param config the configuration params
     */
    public ElevationFacetLayer(Config config) {
        super(ElevationFacet.class, config);
    }

    public ElevationFacetLayer() {
        super(ElevationFacet.class, 0d, 1.0d);
    }
}
