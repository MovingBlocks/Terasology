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

import java.awt.image.BufferedImage;

import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.Renders;

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
