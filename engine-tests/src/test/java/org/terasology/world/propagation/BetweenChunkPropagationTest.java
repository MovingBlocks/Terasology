/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.world.propagation;

import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.propagation.light.InternalLightProcessor;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenWorldView;
import org.terasology.world.propagation.light.SunlightWorldView;

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

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, ChunkConstants.MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, ChunkConstants.MAX_SUNLIGHT_REGEN);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM, true);
        propagator.process();
        sunlightPropagator.process();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            assertEquals(ChunkConstants.MAX_SUNLIGHT, bottomChunk.getSunlight(pos), () -> "Incorrect at position " + pos);
            assertEquals(ChunkConstants.MAX_SUNLIGHT_REGEN, bottomChunk.getSunlightRegen(pos), () -> "Incorrect at position " + pos);
        }
    }

    @Test
    public void testBetweenChunksSimpleSunlightRegenOnly() {
        Chunk topChunk = new ChunkImpl(new Vector3i(0, 1, 0), blockManager, extraDataManager);
        Chunk bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, ChunkConstants.MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, ChunkConstants.MAX_SUNLIGHT_REGEN);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM, true);
        propagator.process();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            assertEquals(ChunkConstants.MAX_SUNLIGHT_REGEN, bottomChunk.getSunlightRegen(pos), () -> "Incorrect at position " + pos);
        }
    }

    @Test
    public void testBetweenChunksWithOverhang() {
        Chunk topChunk = new ChunkImpl(new Vector3i(0, 1, 0), blockManager, extraDataManager);
        Chunk bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, ChunkConstants.MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, ChunkConstants.MAX_SUNLIGHT_REGEN);
        }
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(16, 48, 0), new Vector3i(31, 48, 31))) {
            bottomChunk.setBlock(pos, solid);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);

        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM, false);
        propagator.process();
        sunlightPropagator.process();
        for (int z = 0; z < ChunkConstants.SIZE_Z; ++z) {
            assertEquals(14, bottomChunk.getSunlight(16, 47, z));
        }
        for (int z = 0; z < ChunkConstants.SIZE_Z; ++z) {
            assertEquals(13, bottomChunk.getSunlight(17, 47, z));
        }
    }

    @Test
    public void testPropagateSunlightAppearingMidChunk() {
        Chunk topChunk = new ChunkImpl(new Vector3i(0, 1, 0), blockManager, extraDataManager);
        Chunk bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, (byte) 0);
            topChunk.setSunlightRegen(pos, (byte) 0);
        }
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(8, 0, 8), new Vector3i(ChunkConstants.SIZE_X - 16, 1, ChunkConstants.SIZE_Z - 16))) {
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
        private Map<Vector3i, Chunk> chunks = Maps.newHashMap();

        SelectChunkProvider(Chunk... chunks) {
            for (Chunk chunk : chunks) {
                this.chunks.put(chunk.getPosition(), chunk);
            }
        }

        public void addChunk(Chunk chunk) {
            chunks.put(chunk.getPosition(), chunk);
        }

        @Override
        public ChunkViewCore getLocalView(Vector3i centerChunkPos) {
            return null;
        }

        @Override
        public ChunkViewCore getSubviewAroundBlock(Vector3i blockPos, int extent) {
            return null;
        }

        @Override
        public ChunkViewCore getSubviewAroundChunk(Vector3i chunkPos) {
            return null;
        }

        @Override
        public boolean reloadChunk(Vector3i pos) {
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
        public boolean isChunkReady(Vector3i pos) {
            return false;
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
        public Chunk getChunk(Vector3i chunkPos) {
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
