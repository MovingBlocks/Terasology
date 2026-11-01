// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.joml.Vector2i;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.context.annotation.API;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * A function that can be used as a {@link Zone#regionFunction} to create zones that are layered on top of each other.
 *
 * These layers are ordered according to {@link #ordering}, and have a thickness determined by {@link #layerThickness}.
 */
@API
public class LayeredZoneRegionFunction implements ZoneRegionFunction {

    private List<LayeredZoneRegionFunction> siblings;
    private List<LayeredZoneRegionFunction> abovegroundLayers;
    private List<LayeredZoneRegionFunction> undergroundLayers;
    private ConcurrentMap<Vector2i, LayerRange> layerRangeMap = new ConcurrentHashMap<>(Chunks.SIZE_X * Chunks.SIZE_Z * 100);
    private LayerThickness layerThickness;
    private long seed;
    private Zone parent;

    private final int ordering;

    public LayeredZoneRegionFunction(LayerThickness layerThickness, int ordering) {
        this.layerThickness = layerThickness;
        this.ordering = ordering;
    }

    @Override
    public boolean apply(int x, int y, int z, Region region) {
        return getLayerRange(x, z, region).layerContains(y);
    }

    @Override
    public void initialize(Zone parent) {
        this.parent = parent;

        siblings = Zone.getSiblingRegionFunctions(parent).stream()
                .filter(function -> function instanceof LayeredZoneRegionFunction)
                .map(layerFunction -> (LayeredZoneRegionFunction) layerFunction)
                .sorted(Comparator.comparingInt(layerFunction -> Math.abs(layerFunction.getOrdering())))
                .collect(Collectors.toList());

        undergroundLayers = siblings.stream()
                .filter(LayeredZoneRegionFunction::isUnderground)
                .collect(Collectors.toList());

        abovegroundLayers = siblings.stream()
                .filter(layer -> !layer.isUnderground())
                .collect(Collectors.toList());

        seed = parent.getSeed();
        layerThickness.initialize(this);
    }

    private LayerRange getLayerRange(int x, int z, Region region) {
        Vector2i pos = new Vector2i(x, z);
        if (!layerRangeMap.containsKey(pos)) {
            int surfaceHeight = (int) Math.floor(region.getFacet(ElevationFacet.class).getWorld(pos));

            boolean aboveground = ordering > 0;
            int cumulativeDistanceSmall = 0;
            int cumulativeDistanceLarge = 0;
            LayerRange layerRange = null;

            List<LayeredZoneRegionFunction> layers = aboveground ? abovegroundLayers : undergroundLayers;

            int layerIndex;
            for (layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
                LayeredZoneRegionFunction currentLayer = layers.get(layerIndex);

                int thickness = currentLayer.layerThickness.get(x, z);

                cumulativeDistanceLarge += thickness;
                if (this.equals(currentLayer)) {
                    if (aboveground) {
                        layerRange = new LayerRange()
                                .setMin(surfaceHeight + cumulativeDistanceSmall)
                                .setMax(surfaceHeight + cumulativeDistanceLarge);
                        break;
                    } else {
                        layerRange = new LayerRange()
                                .setMin(surfaceHeight - cumulativeDistanceLarge)
                                .setMax(surfaceHeight - cumulativeDistanceSmall);
                        break;
                    }
                }
                cumulativeDistanceSmall += thickness;
            }

            if (layers.size() <= 0 || layerRange == null) {
                throw new IllegalStateException("Layer for zone '" + parent + "' not found in list of " +
                        (aboveground ? "aboveground" : "underground") + " layers.");
            }

            if (layerIndex == layers.size() - 1) {
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

    public int getOrdering() {
        return ordering;
    }

    public boolean isUnderground() {
        return ordering <= 0;
    }

    public long getSeed() {
        return seed;
    }

    public static final class LayeredZoneOrdering {
        public static final int HIGH_SKY = 400;
        public static final int MEDIUM_SKY = 300;
        public static final int LOW_SKY = 200;
        public static final int ABOVE_GROUND = 100;
        public static final int GROUND = 0;
        public static final int SHALLOW_UNDERGROUND = -100;
        public static final int MEDIUM_UNDERGROUND = -200;
        public static final int DEEP_UNDERGROUND = -300;

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
            boolean satisfiesMin = !min.isPresent() || min.get() < height;
            boolean satisfiesMax = !max.isPresent() || max.get() >= height;

            return satisfiesMin && satisfiesMax;
        }
    }
}
