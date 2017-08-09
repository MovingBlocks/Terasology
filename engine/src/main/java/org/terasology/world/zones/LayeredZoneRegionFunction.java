/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.zones;

import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.module.sandbox.API;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A function that can be used as a {@link Zone#regionFunction} to create zones that are layered on top of each other.
 *
 * These layers are ordered according to {@link #ordering}, and have a width of {@link #minWidth}.
 */
@API
public class LayeredZoneRegionFunction implements ZoneRegionFunction {

    private List<LayeredZoneRegionFunction> siblings;
    private List<LayeredZoneRegionFunction> abovegroundLayers;
    private List<LayeredZoneRegionFunction> undergroundLayers;
    private Map<Vector2i, LayerRange> layerRangeMap = new HashMap<>(ChunkConstants.SIZE_X * ChunkConstants.SIZE_Z * 100);

    public static final class LayeredZoneOrdering {
        public static final int HIGH_SKY = 300;
        public static final int MEDIUM_SKY = 200;
        public static final int LOW_SKY = 100;
        public static final int SURFACE = 0;
        public static final int SHALLOW_UNDERGROUND = -100;
        public static final int MEDIUM_UNDERGROUND = -200;
        public static final int DEEP_UNDERGROUND = -300;

    }

    private final int minWidth;
    private final int maxWidth;
    private final int ordering;

    public LayeredZoneRegionFunction(int minWidth, int maxWidth, int ordering) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.ordering = ordering;
    }

    @Override
    public boolean apply(int x, int y, int z, Region region, Zone zone) {
        return getLayerRange(x, z, region, zone).layerContains(y);
    }

    private LayerRange getLayerRange(int x, int z, Region region, Zone zone) {
        Vector2i pos = new Vector2i(x, z);
        if (!layerRangeMap.containsKey(pos)) {
            int surfaceHeight = (int) Math.floor(region.getFacet(SurfaceHeightFacet.class).getWorld(pos));

            boolean aboveground = ordering >= 0;
            int cumulativeDistanceSmall = 0;
            int cumulativeDistanceLarge = 0;
            LayerRange layerRange = null;

            List<LayeredZoneRegionFunction> layers =
                    aboveground ? getAbovegroundLayers(zone) : getUndergroundLayers(zone);

            int i;
            for (i = 0; i < layers.size(); i++) {
                LayeredZoneRegionFunction layer = layers.get(i);
                //TODO: allow variable-width layers
                cumulativeDistanceLarge += layer.getMinWidth();
                if (this.equals(layer)) {
                    if (aboveground) {
                        layerRange = new LayerRange()
                                .setMin(surfaceHeight + cumulativeDistanceSmall)
                                .setMax(surfaceHeight + cumulativeDistanceLarge);
                        break;
                    } else {
                        layerRange = new LayerRange()
                                .setMin(surfaceHeight -cumulativeDistanceLarge)
                                .setMax(surfaceHeight - cumulativeDistanceSmall);
                        break;
                    }
                }
                cumulativeDistanceSmall += layer.getMinWidth();
            }

            if (layers.size() <= 0 || layerRange == null) {
                throw new IllegalStateException("Layer for zone '" + zone + "' not found in list of " +
                        (aboveground ? "aboveground" : "underground") + " layers.");
            }

            if (i == layers.size() - 1) {
                //At one of the edge layers
                if (aboveground) {
                    layerRange.unsetMax();
                } else {
                    layerRange.unsetMin();
                }
            }
            layerRangeMap.put(pos, layerRange);
        }
        return layerRangeMap.get(pos);
    }

    private List<LayeredZoneRegionFunction> getSiblings(Zone zone) {
        if (siblings == null) {
            siblings = getSiblingRegionFunctions(zone).stream()
                    .filter(f -> f instanceof LayeredZoneRegionFunction)
                    .map(l -> (LayeredZoneRegionFunction) l)
                    .sorted((l1, l2) -> ((Integer) Math.abs(l1.getOrdering())).compareTo(Math.abs(l2.getOrdering())))
                    .collect(Collectors.toList());
        }
        return siblings;
    }

    private List<LayeredZoneRegionFunction> getUndergroundLayers(Zone zone) {
        if (undergroundLayers == null) {
            undergroundLayers = getSiblings(zone).stream()
                    .filter(LayeredZoneRegionFunction::isUnderground)
                    .collect(Collectors.toList());
        }
         return undergroundLayers;
    }

    private List<LayeredZoneRegionFunction> getAbovegroundLayers(Zone zone) {
        if (abovegroundLayers == null) {
            abovegroundLayers = getSiblings(zone).stream()
                    .filter(l -> !l.isUnderground())
                    .collect(Collectors.toList());
        }
        return abovegroundLayers;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getOrdering() {
        return ordering;
    }

    public boolean isUnderground() {
        return ordering < 0;
    }

    private static class LayerRange {
        private Optional<Integer> min = Optional.empty();
        private Optional<Integer> max = Optional.empty();

        public LayerRange setMin(int min) {
            this.min = Optional.of(min);
            return this;
        }

        public LayerRange setMax(int max) {
            this.max = Optional.of(max);
            return this;
        }

        public LayerRange unsetMin() {
            this.min = Optional.empty();
            return this;
        }

        public LayerRange unsetMax() {
            this.max = Optional.empty();
            return this;
        }

        public boolean layerContains(int height) {
            boolean satisfiesMin = !min.isPresent() || min.get() <= height;
            boolean satisfiesMax = !max.isPresent() || max.get() > height;

            return satisfiesMin && satisfiesMax;
        }

    }

}
