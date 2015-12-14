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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.util.Map.Entry;
import java.util.function.Function;

import org.terasology.core.world.generator.facets.FloraFacet;
import org.terasology.core.world.generator.rasterizers.FloraType;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.rendering.nui.Color;
import org.terasology.world.generation.Region;
import org.terasology.world.viewer.color.ColorBlender;
import org.terasology.world.viewer.color.ColorBlenders;
import org.terasology.world.viewer.color.ColorModels;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.Renders;
import org.terasology.world.viewer.layers.ZOrder;

/**
 * Renders the flora coverage based on {@link FloraFacet}.
 */
@Renders(value = FloraFacet.class, order = ZOrder.FLORA)
public class FloraFacetLayer extends AbstractFacetLayer {

    private Function<FloraType, Color> colorFunc = new CoreFloraColors();
    private Function<FloraType, String> labelFunc = Object::toString;

    @Override
    public void render(BufferedImage img, Region region) {
        FloraFacet treeFacet = region.getFacet(FloraFacet.class);

        Graphics2D g = img.createGraphics();
        int width = img.getWidth();
        ColorModel colorModel = img.getColorModel();
        ColorBlender blender = ColorBlenders.forColorModel(ColorModels.RGBA, colorModel);
        DataBufferInt dataBuffer = (DataBufferInt) img.getRaster().getDataBuffer();

        for (Entry<BaseVector3i, FloraType> entry : treeFacet.getRelativeEntries().entrySet()) {
            FloraType treeGen = entry.getValue();
            int wx = entry.getKey().getX();
            int wz = entry.getKey().getZ();
            Color color = colorFunc.apply(treeGen);

            int src = color.rgba();
            int dst = dataBuffer.getElem(wz * width + wx);

            int mix = blender.blend(src, dst);
            dataBuffer.setElem(wz * width + wx, mix);
        }

        g.dispose();
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        FloraFacet floraFacet = region.getFacet(FloraFacet.class);

        Region3i worldRegion = floraFacet.getWorldRegion();
        Region3i relativeRegion = floraFacet.getRelativeRegion();

        int rx = wx - worldRegion.minX() + relativeRegion.minX();
        int rz = wy - worldRegion.minZ() + relativeRegion.minZ();

        for (Entry<BaseVector3i, FloraType> entry : floraFacet.getRelativeEntries().entrySet()) {
            BaseVector3i treePos = entry.getKey();

            if (treePos.getX() == rx && treePos.getZ() == rz) {
                FloraType flora = entry.getValue();
                return labelFunc.apply(flora);
            }
        }

        return "-no vegetation-";
    }

}
