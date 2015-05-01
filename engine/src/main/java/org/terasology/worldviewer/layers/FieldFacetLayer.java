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

package org.terasology.worldviewer.layers;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.base.FieldFacet2D;
import org.terasology.worldviewer.color.Blender;
import org.terasology.worldviewer.color.Blenders;
import org.terasology.worldviewer.color.ColorModels;
import org.terasology.worldviewer.config.FacetConfig;

import com.google.common.math.DoubleMath;

/**
 * Provides info about an {@link FieldFacet2D}.
 * @author Martin Steiger
 */
public class FieldFacetLayer extends AbstractFacetLayer {

    private static final List<Color> GRAYS = IntStream
            .range(0, 256)
            .mapToObj(i -> new Color(i, i, i))
            .collect(Collectors.toList());

    private Config config = new Config();

    /**
     * This can be called only through reflection since Config is private
     * @param config the layer configuration info
     */
    public FieldFacetLayer(Config config) {
        this.config = config;
    }

    public FieldFacetLayer(Class<? extends FieldFacet2D> clazz, double offset, double scale) {
        this.config.clazz = clazz;
        this.config.offset = offset;
        this.config.scale = scale;
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        FieldFacet2D facet = region.getFacet(config.clazz);
        double value = facet.getWorld(wx, wy);
        return String.format("%.2f", value);
    }

    @Override
    public void render(BufferedImage img, Region region) {
        FieldFacet2D facet = region.getFacet(config.clazz);

        int width = img.getWidth();
        int height = img.getHeight();
        ColorModel colorModel = img.getColorModel();
        Blender blender = Blenders.forColorModel(ColorModels.RGBA, colorModel);

        DataBufferInt dataBuffer = (DataBufferInt) img.getRaster().getDataBuffer();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                Color col = getColor(facet, x, z);
                int src = col.rgba();
                int dst = dataBuffer.getElem(z * width + x);
                int mix = blender.add(src, dst);
                dataBuffer.setElem(z * width + x, mix);
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
    protected Color getColor(FieldFacet2D facet, int x, int z) {
        double value = facet.get(x, z);
        if (Double.isFinite(value)) {
            int round = DoubleMath.roundToInt(config.offset + config.scale * value, RoundingMode.HALF_UP);
            int idx = TeraMath.clamp(round, 0, 255);
            return GRAYS.get(idx);
        } else {
            return MISSING;
        }
    }

    @Override
    public Class<? extends WorldFacet> getFacetClass() {
        return config.clazz;
    }

    public double getOffset() {
        return config.offset;
    }

    public double getScale() {
        return config.scale;
    }

    /**
     * @param scale the new scale factor
     */
    public void setScale(double scale) {
        if (scale != config.scale) {
            config.scale = scale;
            notifyObservers();
        }
    }

    /**
     * @param offset the new offset
     */
    public void setOffset(double offset) {
        if (offset != config.offset) {
            config.offset = offset;
            notifyObservers();
        }
    }

    @Override
    public FacetConfig getConfig() {
        return config;
    }

    /**
     * Persistent data
     */
    protected static class Config implements FacetConfig {
        private Class<? extends FieldFacet2D> clazz;

        @Range(min = -100, max = 100, increment = 1f, precision = 1)
        private double offset;

        @Range(min = 0, max = 100, increment = 0.1f, precision = 1)
        private double scale;
    }
}
