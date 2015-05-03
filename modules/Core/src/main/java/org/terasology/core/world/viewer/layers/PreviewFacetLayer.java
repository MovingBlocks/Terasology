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

package org.terasology.core.world.viewer.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.terasology.core.world.generator.facets.World2dPreviewFacet;
import org.terasology.world.generation.Region;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.Renders;

/**
 * @author Martin Steiger
 */
@Renders(value = World2dPreviewFacet.class, order = -1)
public class PreviewFacetLayer extends AbstractFacetLayer {

    public PreviewFacetLayer() {
        setVisible(false);
    }

    @Override
    public void render(BufferedImage img, Region region) {
        World2dPreviewFacet previewFacet = region.getFacet(World2dPreviewFacet.class);

        Graphics2D g = img.createGraphics();
        g.setColor(new Color(previewFacet.getColor().rgba() >> 8));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        return null;
    }

}
