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

package org.terasology.world.viewer.layers;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.util.function.Function;

import org.terasology.rendering.nui.Color;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.base.ObjectFacet2D;
import org.terasology.world.viewer.color.ColorBlender;
import org.terasology.world.viewer.color.ColorBlenders;
import org.terasology.world.viewer.color.ColorModels;

/**
 * Provides info about an {@link ObjectFacet2D}.
 * @param <E> the object type
 */
public abstract class NominalFacetLayer<E> extends AbstractFacetLayer {

    private final Function<? super E, Color> colorMap;
    private final Class<? extends ObjectFacet2D<E>> facetClass;

    public NominalFacetLayer(Class<? extends ObjectFacet2D<E>> clazz, Function<? super E, Color> colorMap) {
        this.colorMap = colorMap;
        this.facetClass = clazz;
    }

    @Override
    public void render(BufferedImage img, Region region) {
        ObjectFacet2D<E> facet = region.getFacet(facetClass);

        int width = img.getWidth();
        int height = img.getHeight();

        ColorModel colorModel = img.getColorModel();
        ColorBlender blender = ColorBlenders.forColorModel(ColorModels.RGBA, colorModel);
        DataBufferInt dataBuffer = (DataBufferInt) img.getRaster().getDataBuffer();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                Color src = getColor(facet, x, z);
                int blend = blender.get(src.rgba());
                dataBuffer.setElem(z * width + x, blend);
            }
        }
    }

    /**
     * Computes the color of the facet at a given world coordinate.
     * @param facet the underlying facet
     * @param x the world x coordinate
     * @param z the world z coordinate
     * @return the color at the given world coords. Never <code>null</code>.
     */
    protected Color getColor(ObjectFacet2D<E> facet, int x, int z) {
        E val = facet.get(x, z);
        if (val == null) {
            return MISSING;
        }

        return colorMap.apply(val);
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        ObjectFacet2D<E> facet = region.getFacet(facetClass);
        E val = facet.getWorld(wx, wy);
        if (val == null) {
            return "<missing>";
        }
        return val.toString();
    }
}
