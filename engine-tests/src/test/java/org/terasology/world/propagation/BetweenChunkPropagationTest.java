// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.family.SymmetricFamily;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.internal.ChunkViewCore;
import org.terasology.engine.world.propagation.BatchPropagator;
import org.terasology.engine.world.propagation.StandardBatchPropagator;
import org.terasology.engine.world.propagation.SunlightRegenBatchPropagator;
import org.terasology.engine.world.propagation.light.InternalLightProcessor;
import org.terasology.engine.world.propagation.light.SunlightPropagationRules;
import org.terasology.engine.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.engine.world.propagation.light.SunlightRegenWorldView;
import org.terasology.engine.world.propagation.light.SunlightWorldView;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BetweenChunkPropagationTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;
    private ExtraBlockDataManager extraDataManager;
    private Block solid;
    private SunlightPropagationRules lightRules;
    private SunlightRegenPropagationRules regenRules;

    private SelectChunkProvider provider = new SelectChunkProvider();

    private SunlightRegenWorldView regenWorldView;
    private SunlightWorldView lightWorldView;

    private BatchPropagator sunlightPropagator;
    private SunlightRegenBatchPropagator propagator;

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);

        regenRules = new SunlightRegenPropagationRules();
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager, true);
        CoreRegistry.put(BlockManager.class, blockManager);
        extraDataManager = new ExtraBlockDataManager();

        BlockFamilyDefinitionData solidData = new BlockFamilyDefinitionData();
        solidData.getBaseSection().setDisplayName("Stone");
        solidData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        solidData.getBaseSection().setTranslucent(false);
        solidData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:stone"), solidData, BlockFamilyDefinition.class);
        solid = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:stone")));

        regenWorldView = new SunlightRegenWorldView(provider);
        lightWorldView = new SunlightWorldView(provider);

        lightRules = new SunlightPropagationRules(regenWorldView);
        sunlightPropagator = new StandardBatchPropagator(lightRules, lightWorldView);
        propagator = new SunlightRegenBatchPropagator(regenRules, regenWorldView, sunlightPropagator, lightWorldView);
    }

    @Test
    public void testBetweenChunksSimple() {
        Chunk topChunk = new ChunkImpl(new Vector3i(0, 1, 0), blockManager, extraDataManager);
        Chunk bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3ic pos : new BlockRegion(0, 0, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            topChunk.setSunlight(pos, Chunks.MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, Chunks.MAX_SUNLIGHT_REGEN);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM, true);
        propagator.process();
        sunlightPropagator.process();
        for (Vector3ic pos : Chunks.CHUNK_REGION) {
            assertEquals(Chunks.MAX_SUNLIGHT, bottomChunk.getSunlight(pos), () -> "Incorrect at position " + pos);
            assertEquals(Chunks.MAX_SUNLIGHT_REGEN, bottomChunk.getSunlightRegen(pos), () -> "Incorrect at position " + pos);
        }
    }

    @Test
    public void testBetweenChunksSimpleSunlightRegenOnly() {
        Chunk topChunk = new ChunkImpl(new Vector3i(0, 1, 0), blockManager, extraDataManager);
        Chunk bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3ic pos : new BlockRegion(0, 0, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            topChunk.setSunlight(pos, Chunks.MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, Chunks.MAX_SUNLIGHT_REGEN);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM, true);
        propagator.process();
        for (Vector3ic pos : Chunks.CHUNK_REGION) {
            assertEquals(Chunks.MAX_SUNLIGHT_REGEN, bottomChunk.getSunlightRegen(pos), () -> "Incorrect at position " + pos);
        }
    }

    @Test
    public void testBetweenChunksWithOverhang() {
        Chunk topChunk = new ChunkImpl(new Vector3i(0, 1, 0), blockManager, extraDataManager);
        Chunk bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3ic pos : new BlockRegion(0, 0, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            topChunk.setSunlight(pos, Chunks.MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, Chunks.MAX_SUNLIGHT_REGEN);
        }
        for (Vector3ic pos : new BlockRegion(16, 48, 0, 31, 48, 31)) {
            bottomChunk.setBlock(pos, solid);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);

        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM, false);
        propagator.process();
        sunlightPropagator.process();
        for (int z = 0; z < Chunks.SIZE_Z; ++z) {
            assertEquals(14, bottomChunk.getSunlight(16, 47, z));
        }
        for (int z = 0; z < Chunks.SIZE_Z; ++z) {
            assertEquals(13, bottomChunk.getSunlight(17, 47, z));
        }
    }

    @Test
    public void testPropagateSunlightAppearingMidChunk() {
        Chunk topChunk = new ChunkImpl(new Vector3i(0, 1, 0), blockManager, extraDataManager);
        Chunk bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3ic pos : new BlockRegion(0, 0, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            topChunk.setSunlight(pos, (byte) 0);
            topChunk.setSunlightRegen(pos, (byte) 0);
        }
        for (Vector3ic pos : new BlockRegion(8, 0, 8).setSize(Chunks.SIZE_X- 16, 1, Chunks.SIZE_Z- 16)) {
            topChunk.setSunlight(pos, (byte) 0);
            topChunk.setSunlightRegen(pos, (byte) 32);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);

        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM, false);
        propagator.process();
        sunlightPropagator.process();
        for (int i = 0; i < 15; ++i) {
            assertEquals(14 - i, bottomChunk.getSunlight(7, 33 + i, 16), "Incorrect value at " + (33 + i));
        }
        for (int i = 2; i < 33; ++i) {
            assertEquals(14, bottomChunk.getSunlight(7, i, 16), "Incorrect value at " + i);
        }
    }

    private static class SelectChunkProvider implements ChunkProvider {
        private Map<Vector3ic, Chunk> chunks = Maps.newHashMap();

        SelectChunkProvider(Chunk... chunks) {
            for (Chunk chunk : chunks) {
                this.chunks.put(chunk.getPosition(new Vector3i()), chunk);
            }
        }

        public void addChunk(Chunk chunk) {
            chunks.put(chunk.getPosition(new Vector3i()), chunk);
        }

        @Override
        public ChunkViewCore getLocalView(Vector3ic centerChunkPos) {
            return null;
        }

        @Override
        public ChunkViewCore getSubviewAroundBlock(Vector3ic blockPos, int extent) {
            return null;
        }

        @Override
        public ChunkViewCore getSubviewAroundChunk(Vector3ic chunkPos) {
            return null;
        }

        @Override
        public boolean reloadChunk(Vector3ic pos) {
            return false;
        }

        @Override
        public void setWorldEntity(EntityRef entity) {
            // do nothing
        }

        @Override
        public Collection<Chunk> getAllChunks() {
            return this.chunks.values();
        }

        @Override
        public void update() {
            // do nothing
        }

        @Override
        public boolean isChunkReady(Vector3ic pos) {
            return false;
        }

        @Override
        public Chunk getChunk(int x, int y, int z) {
            return getChunk(new Vector3i(x, y, z));
        }


        @Override
        public Chunk getChunk(Vector3ic chunkPos) {
            return chunks.get(chunkPos);
        }

        @Override
        public void dispose() {
            // do nothing
        }

        @Override
        public void restart() {
            // do nothing
        }

        @Override
        public void shutdown() {
            // do nothing
        }

        @Override
        public void purgeWorld() {
            // do nothing
        }
    }
}
