/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.NullWorldAtlas;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.propagation.light.InternalLightProcessor;
import org.terasology.world.propagation.light.LightWorldView;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenWorldView;
import org.terasology.world.propagation.light.SunlightWorldView;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class BetweenChunkPropagationTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;
    private Block air;
    private Block solid;
    private SunlightPropagationRules lightRules;
    private SunlightRegenPropagationRules regenRules;

    private SelectChunkProvider provider = new SelectChunkProvider();

    private SunlightRegenWorldView regenWorldView;
    private SunlightWorldView lightWorldView;

    private BatchPropagator sunlightPropagator;
    private SunlightRegenBatchPropagator propagator;


    @Before
    public void setup() throws Exception {
        super.setup();

        regenRules = new SunlightRegenPropagationRules();
        blockManager = new BlockManagerImpl(new NullWorldAtlas(),
                Lists.<String>newArrayList(), Maps.<String, Short>newHashMap(), true, new DefaultBlockFamilyFactoryRegistry());
        CoreRegistry.put(BlockManager.class, blockManager);

        solid = new Block();
        solid.setDisplayName("Solid");
        solid.setUri(new BlockUri("engine:solid"));
        solid.setId((byte) 5);
        for (Side side : Side.values()) {
            solid.setFullSide(side, true);
        }
        blockManager.addBlockFamily(new SymmetricFamily(solid.getURI(), solid), true);

        regenWorldView = new SunlightRegenWorldView(provider);
        lightWorldView = new SunlightWorldView(provider);

        lightRules = new SunlightPropagationRules(regenWorldView);
        sunlightPropagator = new StandardBatchPropagator(lightRules, lightWorldView);
        propagator = new SunlightRegenBatchPropagator(regenRules, regenWorldView, sunlightPropagator, lightWorldView);

        air = BlockManager.getAir();
    }


    @Test
    public void betweenChunksSimple() {
        ChunkImpl topChunk = new ChunkImpl(new Vector3i(0, 1, 0));
        ChunkImpl bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0));

        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, ChunkConstants.MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, ChunkConstants.MAX_SUNLIGHT_REGEN);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM);
        propagator.process();
        sunlightPropagator.process();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            assertEquals("Incorrect at position " + pos, ChunkConstants.MAX_SUNLIGHT, bottomChunk.getSunlight(pos));
            assertEquals("Incorrect at position " + pos, ChunkConstants.MAX_SUNLIGHT_REGEN, bottomChunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void betweenChunksWithOverhang() {
        ChunkImpl topChunk = new ChunkImpl(new Vector3i(0, 1, 0));
        ChunkImpl bottomChunk = new ChunkImpl(new Vector3i(0, 0, 0));

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

        propagator.propagateBetween(topChunk, bottomChunk, Side.BOTTOM);
        propagator.process();
        sunlightPropagator.process();
        assertEquals(14, bottomChunk.getSunlight(16, 47, 0));
    }

    private static class SelectChunkProvider implements ChunkProvider {
        private Map<Vector3i, ChunkImpl> chunks = Maps.newHashMap();

        public SelectChunkProvider(ChunkImpl ... chunks) {
            for (ChunkImpl chunk : chunks) {
                this.chunks.put(chunk.getPos(), chunk);
            }
        }

        public void addChunk(ChunkImpl chunk) {
            chunks.put(chunk.getPos(), chunk);
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
        public void setWorldEntity(EntityRef entity) {

        }

        @Override
        public void addRelevanceEntity(EntityRef entity, Vector3i distance) {

        }

        @Override
        public void addRelevanceEntity(EntityRef entity, Vector3i distance, ChunkRegionListener listener) {

        }

        @Override
        public void updateRelevanceEntity(EntityRef entity, Vector3i distance) {

        }

        @Override
        public void removeRelevanceEntity(EntityRef entity) {

        }

        @Override
        public void update() {

        }

        @Override
        public boolean isChunkReady(Vector3i pos) {
            return false;
        }

        @Override
        public ChunkImpl getChunk(int x, int y, int z) {
            return getChunk(new Vector3i(x, y, z));
        }

        @Override
        public ChunkImpl getChunk(Vector3i chunkPos) {
            return chunks.get(chunkPos);
        }

        @Override
        public void dispose() {

        }

        @Override
        public void purgeChunks() {

        }
    }
}
