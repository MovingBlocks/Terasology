// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.layers;

import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.facets.base.ObjectFacet2D;
import org.terasology.nui.Color;
import org.terasology.engine.world.viewer.color.ColorBlender;
import org.terasology.engine.world.viewer.color.ColorBlenders;
import org.terasology.engine.world.viewer.color.ColorModels;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.util.function.Function;

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
                if (src != null) {
                    int blend = blender.get(src.rgba());
                    dataBuffer.setElem(z * width + x, blend);
                }
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
