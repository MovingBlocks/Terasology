// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.viewer.layers.engine;

import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.Renders;

import java.awt.image.BufferedImage;

/**
 * Provides information about the sea level.
 */
@Renders(value = SeaLevelFacet.class, order = -1)
public class SeaLevelFacetLayer extends AbstractFacetLayer {

    @Override
    public void render(BufferedImage img, Region region) {
        // ignore
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        return "SeaLevel: " + region.getFacet(SeaLevelFacet.class).getSeaLevel();
    }

}
