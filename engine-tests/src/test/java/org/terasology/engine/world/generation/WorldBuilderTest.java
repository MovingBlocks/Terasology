// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.facets.base.BaseFacet3D;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldBuilderTest {

    private Context context = new ContextImpl();

    @Test
    public void testBorderCalculation() {
        WorldBuilder worldBuilder = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        worldBuilder.setSeed(12);
        worldBuilder.addProvider(new Facet2Provider());
        worldBuilder.addProvider(new Facet1Provider());

        World world = worldBuilder.build();
        BlockRegion regionToGenerate = new BlockRegion(0, 0, 0).expand(1, 1, 1);
        Region regionData = world.getWorldData(regionToGenerate);

        Facet1 facet1 = regionData.getFacet(Facet1.class);
        assertEquals(regionToGenerate, facet1.getWorldRegion());

        Facet2 facet2 = regionData.getFacet(Facet2.class);
        assertEquals(new BlockRegion(-3, -1, -3).setSize(7, 3, 7), facet2.getWorldRegion());
    }

    @Test
    public void testCumulativeBorderCalculation() {
        WorldBuilder worldBuilder = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        worldBuilder.setSeed(12);
        worldBuilder.addProvider(new Facet2Provider());
        worldBuilder.addProvider(new Facet1Provider());
        worldBuilder.addProvider(new Facet3Provider());

        World world = worldBuilder.build();
        BlockRegion regionToGenerate = new BlockRegion(0, 0, 0).expand(1, 1, 1);
        Region regionData = world.getWorldData(regionToGenerate);

        Facet3 facet3 = regionData.getFacet(Facet3.class);
        assertEquals(regionToGenerate, facet3.getWorldRegion());

        Facet1 facet1 = regionData.getFacet(Facet1.class);
        assertEquals(new BlockRegion(-2, -1, -2).setSize(5, 3, 5), facet1.getWorldRegion());

        Facet2 facet2 = regionData.getFacet(Facet2.class);
        assertEquals(new BlockRegion(-4, -1, -4).setSize(9, 3, 9), facet2.getWorldRegion());
    }

    @Test
    public void testMultiplePathsBorderCalculation() {
        WorldBuilder worldBuilder = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        worldBuilder.setSeed(12);
        worldBuilder.addProvider(new Facet2Provider());
        worldBuilder.addProvider(new Facet1Provider());
        worldBuilder.addProvider(new Facet4Provider());

        World world = worldBuilder.build();
        BlockRegion regionToGenerate = new BlockRegion(0, 0, 0).expand(1, 1, 1);
        Region regionData = world.getWorldData(regionToGenerate);

        Facet1 facet1 = regionData.getFacet(Facet1.class);
        assertEquals(regionToGenerate, facet1.getWorldRegion());

        Facet4 facet4 = regionData.getFacet(Facet4.class);
        assertEquals(regionToGenerate, facet4.getWorldRegion());

        Facet2 facet2 = regionData.getFacet(Facet2.class);
        assertEquals(new BlockRegion(-4, -1, -4).setSize(9, 3, 9), facet2.getWorldRegion());
    }


    @Test
    public void testUpdating() {
        WorldBuilder worldBuilder = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        worldBuilder.setSeed(12);
        worldBuilder.addProvider(new Facet2Provider());
        worldBuilder.addProvider(new Facet1Provider());
        worldBuilder.addProvider(new Facet3Provider());
        worldBuilder.addProvider(new Facet4Provider());
        worldBuilder.addProvider(new FacetUpdater());

        BlockRegion regionToGenerate = new BlockRegion(0, 0, 0).expand(1, 1, 1);

        World world;
        Region regionData;

        // try checking updated facet
        world = worldBuilder.build();
        regionData = world.getWorldData(regionToGenerate);
        assertTrue(regionData.getFacet(Facet1.class).updated);
        assertTrue(regionData.getFacet(Facet4.class).updated);

        // try checking generated facet
        world = worldBuilder.build();
        regionData = world.getWorldData(regionToGenerate);
        assertNotNull(regionData.getFacet(Facet3.class));
        assertTrue(regionData.getFacet(Facet4.class).updated);
    }

    @Test
    public void testProviderRegistrationOrderDoesNotMatter() {
        // Providers reach WorldBuilder in whatever order the module/class scan hands them over, which is
        // hash-based and varies between JVM runs. Registering a consumer before its producer must therefore
        // still build a valid world rather than reporting the producer as missing.
        WorldBuilder consumerFirst = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        consumerFirst.setSeed(12);
        consumerFirst.addProvider(new Facet1Provider()); // requires Facet2
        consumerFirst.addProvider(new Facet2Provider()); // produces Facet2

        WorldBuilder producerFirst = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        producerFirst.setSeed(12);
        producerFirst.addProvider(new Facet2Provider());
        producerFirst.addProvider(new Facet1Provider());

        BlockRegion regionToGenerate = new BlockRegion(0, 0, 0).expand(1, 1, 1);

        Region consumerFirstData = consumerFirst.build().getWorldData(regionToGenerate);
        Region producerFirstData = producerFirst.build().getWorldData(regionToGenerate);

        // Both orders must agree, and must agree with the borders asserted by testBorderCalculation.
        assertEquals(regionToGenerate, consumerFirstData.getFacet(Facet1.class).getWorldRegion());
        assertEquals(producerFirstData.getFacet(Facet1.class).getWorldRegion(),
                consumerFirstData.getFacet(Facet1.class).getWorldRegion());
        assertEquals(producerFirstData.getFacet(Facet2.class).getWorldRegion(),
                consumerFirstData.getFacet(Facet2.class).getWorldRegion());
    }

    @Test
    public void testGenuinelyMissingProviderStillThrows() {
        // Order independence must not weaken the real check: a required facet that nothing produces
        // or updates is still a configuration error, whatever order the providers arrived in.
        WorldBuilder worldBuilder = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        worldBuilder.setSeed(12);
        worldBuilder.addProvider(new Facet1Provider()); // requires Facet2, which nothing here provides

        assertThrows(IllegalStateException.class, worldBuilder::build);
    }

    @Test
    public void testRequiredFacetCoversUpdatedFacet() {
        // Mirrors the CoreWorlds/Caves arrangement behind Terasology/CoreWorlds#48:
        //   DensityUpdater  updates Density, requires Roughness with no border of its own
        //   SurfaceSpreader updates Spread,  requires Density with a sides border
        // The sides border SurfaceSpreader asks for on Density has to reach Roughness too, otherwise
        // DensityUpdater iterates a Density region wider than the Roughness it reads per column and
        // walks off the end of it.
        WorldBuilder worldBuilder = new WorldBuilder(context.get(WorldGeneratorPluginLibrary.class));
        worldBuilder.setSeed(12);
        worldBuilder.addProvider(new RoughnessProvider());
        worldBuilder.addProvider(new DensityProvider());
        worldBuilder.addProvider(new DensityUpdater());
        worldBuilder.addProvider(new SpreadProvider());
        worldBuilder.addProvider(new SurfaceSpreader());

        BlockRegion regionToGenerate = new BlockRegion(0, 0, 0).expand(1, 1, 1);
        Region regionData = worldBuilder.build().getWorldData(regionToGenerate);

        BlockRegion density = regionData.getFacet(DensityLike.class).getWorldRegion();
        BlockRegion roughness = regionData.getFacet(RoughnessLike.class).getWorldRegion();

        assertTrue(roughness.getSizeX() >= density.getSizeX() && roughness.getSizeZ() >= density.getSizeZ(),
                "Roughness " + roughness + " must cover every column of Density " + density
                        + "; DensityUpdater reads one from the other by world coordinate.");
    }

    public static class DensityLike extends BaseFacet3D {
        public DensityLike(BlockRegion targetRegion, Border3D border) {
            super(targetRegion, border);
        }
    }

    public static class RoughnessLike extends BaseFacet3D {
        public RoughnessLike(BlockRegion targetRegion, Border3D border) {
            super(targetRegion, border);
        }
    }

    public static class SpreadLike extends BaseFacet3D {
        public SpreadLike(BlockRegion targetRegion, Border3D border) {
            super(targetRegion, border);
        }
    }

    @Produces(RoughnessLike.class)
    public static class RoughnessProvider implements FacetProvider {
        @Override
        public void process(GeneratingRegion region) {
            region.setRegionFacet(RoughnessLike.class,
                    new RoughnessLike(region.getRegion(), region.getBorderForFacet(RoughnessLike.class)));
        }
    }

    @Produces(DensityLike.class)
    public static class DensityProvider implements FacetProvider {
        @Override
        public void process(GeneratingRegion region) {
            region.setRegionFacet(DensityLike.class,
                    new DensityLike(region.getRegion(), region.getBorderForFacet(DensityLike.class)));
        }
    }

    @Produces(SpreadLike.class)
    public static class SpreadProvider implements FacetProvider {
        @Override
        public void process(GeneratingRegion region) {
            region.setRegionFacet(SpreadLike.class,
                    new SpreadLike(region.getRegion(), region.getBorderForFacet(SpreadLike.class)));
        }
    }

    /** Stands in for CoreWorlds' DensityNoiseProvider. */
    @Requires(@Facet(RoughnessLike.class))
    @Updates(@Facet(value = DensityLike.class, border = @FacetBorder(top = 1)))
    public static class DensityUpdater implements FacetProvider {
        @Override
        public void process(GeneratingRegion region) {
        }
    }

    /** Stands in for Caves' CaveToSurfaceProvider. */
    @Requires(@Facet(value = DensityLike.class, border = @FacetBorder(sides = 3)))
    @Updates(@Facet(value = SpreadLike.class, border = @FacetBorder(sides = 3)))
    public static class SurfaceSpreader implements FacetProvider {
        @Override
        public void process(GeneratingRegion region) {
        }
    }

    public static class Facet1 extends BaseFacet3D {
        public boolean updated;

        public Facet1(BlockRegion targetRegion, Border3D border) {
            super(targetRegion, border);
        }
    }

    public static class Facet2 extends BaseFacet3D {
        public Facet2(BlockRegion targetRegion, Border3D border) {
            super(targetRegion, border);
        }
    }

    public static class Facet3 extends BaseFacet3D {
        public Facet3(BlockRegion targetRegion, Border3D border) {
            super(targetRegion, border);
        }
    }

    public static class Facet4 extends BaseFacet3D {
        public boolean updated;

        public Facet4(BlockRegion targetRegion, Border3D border) {
            super(targetRegion, border);

        }
    }

    @Produces(Facet1.class)
    @Requires(@Facet(value = Facet2.class, border = @FacetBorder(sides = 2)))
    public static class Facet1Provider implements FacetProvider {

        @Override
        public void process(GeneratingRegion region) {
            Facet1 facet = new Facet1(region.getRegion(), region.getBorderForFacet(Facet1.class));
            region.setRegionFacet(Facet1.class, facet);
        }
    }

    @Produces(Facet2.class)
    public static class Facet2Provider implements FacetProvider {

        @Override
        public void process(GeneratingRegion region) {
            Facet2 facet = new Facet2(region.getRegion(), region.getBorderForFacet(Facet2.class));
            region.setRegionFacet(Facet2.class, facet);
        }
    }

    @Produces(Facet3.class)
    @Requires(@Facet(value = Facet1.class, border = @FacetBorder(sides = 1)))
    public static class Facet3Provider implements FacetProvider {

        @Override
        public void process(GeneratingRegion region) {
            Facet3 facet = new Facet3(region.getRegion(), region.getBorderForFacet(Facet3.class));
            region.setRegionFacet(Facet3.class, facet);
        }
    }

    @Produces(Facet4.class)
    @Requires(@Facet(value = Facet2.class, border = @FacetBorder(sides = 3)))
    public static class Facet4Provider implements FacetProvider {

        @Override
        public void process(GeneratingRegion region) {
            Facet4 facet = new Facet4(region.getRegion(), region.getBorderForFacet(Facet4.class));
            region.setRegionFacet(Facet4.class, facet);
        }
    }

    @Requires(@Facet(Facet2.class))
    @Produces(Facet3.class)
    @Updates({@Facet(Facet1.class), @Facet(Facet4.class)})
    public static class FacetUpdater implements FacetProvider {

        @Override
        public void process(GeneratingRegion region) {
            Facet3 facet = new Facet3(region.getRegion(), region.getBorderForFacet(Facet3.class));
            Facet1 facet1 = region.getRegionFacet(Facet1.class);
            Facet4 facet4 = region.getRegionFacet(Facet4.class);
            facet1.updated = true;
            facet4.updated = true;
            region.setRegionFacet(Facet3.class, facet);
        }
    }
}
