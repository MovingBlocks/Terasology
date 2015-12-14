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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.core.world.generator.trees.TreeGenerator;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector2f;
import org.terasology.world.generation.Region;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.Renders;
import org.terasology.world.viewer.layers.ZOrder;
import org.terasology.world.viewer.picker.CirclePicker;
import org.terasology.world.viewer.picker.CirclePickerAll;

/**
 * Renders the tree coverage based on {@link TreeFacet}
 * and provides aggregating tool tips.
 */
@Renders(value = TreeFacet.class, order = ZOrder.TREES)
public class TreeFacetLayer extends AbstractFacetLayer {

    private Function<TreeGenerator, Integer> radiusFunc = ignore -> 5;
    private Function<TreeGenerator, Color> colorFunc = ignore -> Color.GREEN.darker();
    private Function<TreeGenerator, String> labelFunc = ignore -> "Tree";

    @Override
    public void render(BufferedImage img, Region region) {
        TreeFacet treeFacet = region.getFacet(TreeFacet.class);

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Entry<BaseVector3i, TreeGenerator> entry : treeFacet.getRelativeEntries().entrySet()) {
            TreeGenerator treeGen = entry.getValue();
            int wx = entry.getKey().getX();
            int wz = entry.getKey().getZ();
            int r = radiusFunc.apply(treeGen);
            Color color = colorFunc.apply(treeGen);

            // the fill area is offset by +1/+1 pixel
            // otherwise it will bleed out at the top left corner
            g.setColor(color);
            g.fillOval(wx - r + 1, wz - r + 1, r * 2 - 1, r * 2 - 1);
            g.setColor(color.darker());
            g.drawOval(wx - r, wz - r, r * 2, r * 2);
        }

        g.dispose();
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        TreeFacet treeFacet = region.getFacet(TreeFacet.class);

        Region3i worldRegion = treeFacet.getWorldRegion();
        Region3i relativeRegion = treeFacet.getRelativeRegion();

        int rx = wx - worldRegion.minX() + relativeRegion.minX();
        int rz = wy - worldRegion.minZ() + relativeRegion.minZ();

        Vector2f relCursor = new Vector2f(rx, rz);
        CirclePicker<TreeGenerator> picker = new CirclePickerAll<>(relCursor, radiusFunc);

        for (Entry<BaseVector3i, TreeGenerator> entry : treeFacet.getRelativeEntries().entrySet()) {
            TreeGenerator treeGen = entry.getValue();
            BaseVector3i treePos = entry.getKey();

            picker.offer(treePos.getX(), treePos.getZ(), treeGen);
        }

        Set<TreeGenerator> picked = picker.getAll();

        // try to exit early first
        if (picked.isEmpty()) {
            return null;
        }

        if (picked.size() == 1) {
            TreeGenerator first = picked.iterator().next();
            return labelFunc.apply(first);
        }

        // convert to a stream of labels
        Stream<String> labels = picked.stream().map(labelFunc);

        // collect identical String elements and collect the count in a map
        Map<String, Long> counters = labels.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // define a mapping from a map entry to a String representation
        // TODO: treat 1x occurrences like above (e.g. Tree instead of 1x Tree)
        Function<Entry<String, Long>, String> toStringFunc = e -> String.format("%dx %s", e.getValue(), e.getKey());

        // apply that mapping and join the Strings with a comma
        return counters.entrySet().stream().map(toStringFunc).collect(Collectors.joining(", "));
    }

}
