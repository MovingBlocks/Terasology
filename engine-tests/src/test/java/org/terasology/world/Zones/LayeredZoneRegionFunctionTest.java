// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.Zones;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.joml.Vector2ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.RegionImpl;
import org.terasology.engine.world.generation.WorldFacet;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.zones.LayeredZoneRegionFunction;
import org.terasology.engine.world.zones.MinMaxLayerThickness;
import org.terasology.engine.world.zones.Zone;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.terasology.engine.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.ABOVE_GROUND;
import static org.terasology.engine.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.GROUND;
import static org.terasology.engine.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.LOW_SKY;
import static org.terasology.engine.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.MEDIUM_SKY;
import static org.terasology.engine.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.MEDIUM_UNDERGROUND;
import static org.terasology.engine.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.SHALLOW_UNDERGROUND;

public class LayeredZoneRegionFunctionTest {

    private Zone parent = new Zone("Parent", () -> true);
    private Region region;

    @BeforeEach
    public void setup() {
        parent.addZone(new Zone("Medium sky", new LayeredZoneRegionFunction(new MinMaxLayerThickness(100, 100), MEDIUM_SKY)))
                .addZone(new Zone("Low sky", new LayeredZoneRegionFunction(new MinMaxLayerThickness(100, 100), LOW_SKY)))
                .addZone(new Zone("Above ground", new LayeredZoneRegionFunction(new MinMaxLayerThickness(100, 100), ABOVE_GROUND)))
                .addZone(new Zone("Ground", new LayeredZoneRegionFunction(new MinMaxLayerThickness(100, 100), GROUND)))
                .addZone(new Zone("Shallow underground", new LayeredZoneRegionFunction(new MinMaxLayerThickness(100, 100), SHALLOW_UNDERGROUND)))
                .addZone(new Zone("Medium underground", new LayeredZoneRegionFunction(new MinMaxLayerThickness(100, 100), MEDIUM_UNDERGROUND)));
        parent.setSeed(12345);
        parent.initialize();

        ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains = ArrayListMultimap.create();

        facetProviderChains.put(ElevationFacet.class, (generatingRegion) -> {
                    ElevationFacet facet = new ElevationFacet(generatingRegion.getRegion(),
                            generatingRegion.getBorderForFacet(ElevationFacet.class));

                    for (Vector2ic pos : facet.getRelativeArea()) {
                        facet.set(pos, 100);
                    }

                    generatingRegion.setRegionFacet(ElevationFacet.class, facet);
                });

        Map<Class<? extends WorldFacet>, Border3D> borders = new HashMap<>();
        borders.put(ElevationFacet.class, new Border3D(0, 0, 0));

        region = new RegionImpl(new BlockRegion(0, 0, 0).expand(4, 4, 4),
                facetProviderChains, borders, 1);
    }

    @Test
    public void testCreation() {
        int minWidth = 100;
        int maxWidth = 200;
        int ordering = 1000;

        LayeredZoneRegionFunction function = new LayeredZoneRegionFunction(new MinMaxLayerThickness(minWidth, maxWidth), ordering);

        assertEquals(ordering, function.getOrdering());
    }

    @Test
    public void testSurface() {
        assertTrue(parent.getChildZone("Ground").containsBlock(0, 100, 0, region));
        assertTrue(parent.getChildZone("Ground").containsBlock(0, 1, 0, region));
        assertFalse(parent.getChildZone("Ground").containsBlock(0, 101, 0, region));
        assertFalse(parent.getChildZone("Ground").containsBlock(0, 0, 0, region));
        assertTrue(parent.getChildZone("Above ground").containsBlock(0, 101, 0, region));
        assertTrue(parent.getChildZone("Above ground").containsBlock(0, 200, 0, region));
        assertFalse(parent.getChildZone("Above ground").containsBlock(0, 100, 0, region));
        assertFalse(parent.getChildZone("Above ground").containsBlock(0, 201, 0, region));
    }

    @Test
    public void testUnderground() {
        assertTrue(parent.getChildZone("Shallow underground").containsBlock(0, 0, 0, region));
        assertTrue(parent.getChildZone("Shallow underground").containsBlock(0, -99, 0, region));
        assertFalse(parent.getChildZone("Shallow underground").containsBlock(0, 1, 0, region));
        assertFalse(parent.getChildZone("Shallow underground").containsBlock(0, -100, 0, region));
    }

    @Test
    public void testSky() {
        assertTrue(parent.getChildZone("Low sky").containsBlock(0, 201, 0, region));
        assertTrue(parent.getChildZone("Low sky").containsBlock(0, 300, 0, region));
        assertFalse(parent.getChildZone("Low sky").containsBlock(0, 200, 0, region));
        assertFalse(parent.getChildZone("Low sky").containsBlock(0, 301, 0, region));
    }

    @Test
    public void testExtremes() {
        //Test values at the extremes (beyond the top and bottom of the declared layers
        //The last layer in each direction should extend outwards
        assertTrue(parent.getChildZone("Medium sky").containsBlock(0, 10000, 0, region));
        assertTrue(parent.getChildZone("Medium underground").containsBlock(0, -10000, 0, region));
        assertFalse(parent.getChildZone("Medium sky").containsBlock(0, -10000, 0, region));
        assertFalse(parent.getChildZone("Medium underground").containsBlock(0, 10000, 0, region));
    }

}
