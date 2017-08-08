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
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.RegionImpl;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.zones.LayeredZoneManager;
import org.terasology.world.zones.LayeredZoneRegionFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.LOW_SKY;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.MEDIUM_SKY;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.MEDIUM_UNDERGROUND;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.SHALLOW_UNDERGROUND;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.SURFACE;

public class LayeredZoneRegionFunctionTest {

    private final List<LayeredZoneRegionFunction> functions = new ArrayList<>();
    private Region region;

    @Before
    public void setup() {
        LayeredZoneManager manager = new LayeredZoneManager();
        functions.add(new LayeredZoneRegionFunction(100, 100, SURFACE, manager));
        functions.add(new LayeredZoneRegionFunction(100, 100, LOW_SKY, manager));
        functions.add(new LayeredZoneRegionFunction(100, 100, MEDIUM_SKY, manager));
        functions.add(new LayeredZoneRegionFunction(100, 100, SHALLOW_UNDERGROUND, manager));
        functions.add(new LayeredZoneRegionFunction(100, 100, MEDIUM_UNDERGROUND, manager));


        ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains = ArrayListMultimap.create();

        facetProviderChains.put(SurfaceHeightFacet.class, (generatingRegion) -> {
                    SurfaceHeightFacet facet = new SurfaceHeightFacet(generatingRegion.getRegion(),
                            generatingRegion.getBorderForFacet(SurfaceHeightFacet.class));

                    for (BaseVector2i pos : facet.getRelativeRegion().contents()) {
                        facet.set(pos, 0);
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

        LayeredZoneManager manager = new LayeredZoneManager();
        LayeredZoneRegionFunction function = new LayeredZoneRegionFunction(minWidth, maxWidth, ordering, manager);

        assertEquals(minWidth, function.getMinWidth());
        assertEquals(maxWidth, function.getMaxWidth());
        assertEquals(ordering, function.getOrdering());
    }

    @Test
    public void testApply() {
        //Test values in the surface layer
        assertTrue(functions.get(0).apply(new Vector3i(0, 0, 0), region));
        assertTrue(functions.get(0).apply(new Vector3i(0, 99, 0), region));
        assertFalse(functions.get(0).apply(new Vector3i(0, -1, 0), region));
        assertFalse(functions.get(0).apply(new Vector3i(0, 100, 0), region));


        //Test values in the shallow underground layer
        assertTrue(functions.get(3).apply(new Vector3i(0, -1, 0), region));
        assertTrue(functions.get(3).apply(new Vector3i(0, -100, 0), region));
        assertFalse(functions.get(3).apply(new Vector3i(0, 0, 0), region));
        assertFalse(functions.get(3).apply(new Vector3i(0, -101, 0), region));

        //Test values at the extremes (beyond the top and bottom of the declared layers
        //The last zone in each direction should extend outwards
        assertTrue(functions.get(2).apply(new Vector3i(0, 10000, 0), region));
        assertTrue(functions.get(4).apply(new Vector3i(0, -10000, 0), region));
        assertFalse(functions.get(2).apply(new Vector3i(0, -10000, 0), region));
        assertFalse(functions.get(4).apply(new Vector3i(0, 10000, 0), region));
    }

}
