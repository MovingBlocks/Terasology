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
package org.terasology.world.Zones;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.Before;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.RegionImpl;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.world.zones.ProviderStore;
import org.terasology.world.zones.LayeredZoneRegionFunction;
import org.terasology.world.zones.Zone;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.LOW_SKY;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.MEDIUM_SKY;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.MEDIUM_UNDERGROUND;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.SHALLOW_UNDERGROUND;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.SURFACE;

public class LayeredZoneRegionFunctionTest {

    private ProviderStore parent;
    private Region region;

    @Before
    public void setup() {
        Context context = new ContextImpl();
        parent = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class))
                .addZone(new Zone("Surface", new LayeredZoneRegionFunction(100, 100, SURFACE)))
                .addZone(new Zone("Low sky", new LayeredZoneRegionFunction(100, 100, LOW_SKY)))
                .addZone(new Zone("Medium sky", new LayeredZoneRegionFunction(100, 100, MEDIUM_SKY)))
                .addZone(new Zone("Shallow underground", new LayeredZoneRegionFunction(100, 100, SHALLOW_UNDERGROUND)))
                .addZone(new Zone("Medium underground", new LayeredZoneRegionFunction(100, 100, MEDIUM_UNDERGROUND)));

        ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains = ArrayListMultimap.create();

        facetProviderChains.put(SurfaceHeightFacet.class, (generatingRegion) -> {
                    SurfaceHeightFacet facet = new SurfaceHeightFacet(generatingRegion.getRegion(),
                            generatingRegion.getBorderForFacet(SurfaceHeightFacet.class));

                    for (BaseVector2i pos : facet.getRelativeRegion().contents()) {
                        facet.set(pos, 100);
                    }

                    generatingRegion.setRegionFacet(SurfaceHeightFacet.class, facet);
                });

        Map<Class<? extends WorldFacet>, Border3D> borders = new HashMap<>();
        borders.put(SurfaceHeightFacet.class, new Border3D(0, 0, 0));

        region = new RegionImpl(Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), 4),
                facetProviderChains, borders);
    }

    @Test
    public void testCreation() {
        int minWidth = 100;
        int maxWidth = 200;
        int ordering = 1000;

        LayeredZoneRegionFunction function = new LayeredZoneRegionFunction(minWidth, maxWidth, ordering);

        assertEquals(minWidth, function.getMinWidth());
        assertEquals(maxWidth, function.getMaxWidth());
        assertEquals(ordering, function.getOrdering());
    }

    @Test
    public void testSurface() {
        assertTrue(parent.getChildZone("Surface").containsBlock(new Vector3i(0, 100, 0), region));
        assertTrue(parent.getChildZone("Surface").containsBlock(new Vector3i(0, 199, 0), region));
        assertFalse(parent.getChildZone("Surface").containsBlock(new Vector3i(0, 99, 0), region));
        assertFalse(parent.getChildZone("Surface").containsBlock(new Vector3i(0, 200, 0), region));
    }

    @Test
    public void testUnderground() {
        assertTrue(parent.getChildZone("Shallow underground").containsBlock(new Vector3i(0, 99, 0), region));
        assertTrue(parent.getChildZone("Shallow underground").containsBlock(new Vector3i(0, 0, 0), region));
        assertFalse(parent.getChildZone("Shallow underground").containsBlock(new Vector3i(0, 100, 0), region));
        assertFalse(parent.getChildZone("Shallow underground").containsBlock(new Vector3i(0, -1, 0), region));
    }

    @Test
    public void testExtremes() {
        //Test values at the extremes (beyond the top and bottom of the declared layers
        //The last layer in each direction should extend outwards
        assertTrue(parent.getChildZone("Medium sky").containsBlock(new Vector3i(0, 10000, 0), region));
        assertTrue(parent.getChildZone("Medium underground").containsBlock(new Vector3i(0, -10000, 0), region));
        assertFalse(parent.getChildZone("Medium sky").containsBlock(new Vector3i(0, -10000, 0), region));
        assertFalse(parent.getChildZone("Medium underground").containsBlock(new Vector3i(0, 10000, 0), region));
    }

}
