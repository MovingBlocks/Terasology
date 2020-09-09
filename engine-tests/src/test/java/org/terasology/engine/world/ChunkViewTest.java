// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.family.SymmetricFamily;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkConstants;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.internal.ChunkViewCore;
import org.terasology.engine.world.internal.ChunkViewCoreImpl;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.math.geom.Vector3i;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkViewTest extends TerasologyTestingEnvironment {

    Block airBlock;
    Block solidBlock;
    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;

    @BeforeEach
    public void setup() throws IOException {
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager);
        CoreRegistry.put(BlockManager.class, blockManager);
        airBlock = blockManager.getBlock(BlockManager.AIR_ID);

        extraDataManager = new ExtraBlockDataManager();

        BlockFamilyDefinitionData solidData = new BlockFamilyDefinitionData();
        solidData.getBaseSection().setDisplayName("Stone");
        solidData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        solidData.getBaseSection().setTranslucent(false);
        solidData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:stone"), solidData, BlockFamilyDefinition.class);
        solidBlock = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:stone")));
    }

    @Test
    public void testSimpleWorldView() {
        Chunk chunk = createChunk(0, 0, 0);
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        ChunkViewCore chunkView = new ChunkViewCoreImpl(new Chunk[]{chunk},
                Region3i.createFromCenterExtents(Vector3i.zero(), Vector3i.zero()), new Vector3i(), airBlock);
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void testOffsetWorldView() {
        Chunk chunk = createChunk(0, 0, 0);
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{createChunk(-1, 0, -1), createChunk(0, 0, -1), createChunk(1, 0, -1),
                createChunk(-1, 0, 0), chunk, createChunk(1, 0, 0),
                createChunk(-1, 0, 1), createChunk(0, 0, 1), createChunk(1, 0, 1)};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks,
                Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 0, 1)
                , airBlock);
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void testOffsetWorldViewBeforeMainChunk() {
        Chunk chunk = createChunk(0, 0, 0);
        chunk.setBlock(new Vector3i(ChunkConstants.SIZE_X - 1, 0, ChunkConstants.SIZE_Z - 1), solidBlock);

        Chunk[] chunks = new Chunk[]{chunk, createChunk(0, 0, -1), createChunk(1, 0, -1),
                createChunk(-1, 0, 0), createChunk(0, 0, 0), createChunk(1, 0, 0),
                createChunk(-1, 0, 1), createChunk(0, 0, 1), createChunk(1, 0, 1)};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks,
                Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 0, 1)
                , airBlock);
        assertEquals(solidBlock, chunkView.getBlock(-1, 0, -1));
    }

    @Test
    public void testOffsetWorldViewAfterMainChunk() {
        Chunk chunk = createChunk(0, 0, 0);
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{createChunk(-1, 0, -1), createChunk(0, 0, -1), createChunk(1, 0, -1),
                createChunk(-1, 0, 0), createChunk(0, 0, 0), createChunk(1, 0, 0),
                createChunk(-1, 0, 1), createChunk(0, 0, 1), chunk};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks,
                Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 0, 1)
                , airBlock);
        assertEquals(solidBlock, chunkView.getBlock(ChunkConstants.SIZE_X, 0, ChunkConstants.SIZE_Z));
    }

    @Test
    public void testOffsetChunksWorldView() {
        Chunk chunk = createChunk(1, 0, 1);
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{createChunk(0, 0, 0), createChunk(1, 0, 0), createChunk(2, 0, 0),
                createChunk(0, 0, 1), chunk, createChunk(2, 0, 1),
                createChunk(0, 0, 2), createChunk(1, 0, 2), createChunk(2, 0, 2)};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks,
                Region3i.createFromCenterExtents(new Vector3i(1, 0, 1), new Vector3i(1, 0, 1)), new Vector3i(1, 0, 1)
                , airBlock);
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void testLocalToWorld() {
        Chunk chunk = createChunk(1, 0, 1);
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{createChunk(0, 0, 0), createChunk(1, 0, 0), createChunk(2, 0, 0),
                createChunk(0, 0, 1), chunk, createChunk(2, 0, 1),
                createChunk(0, 0, 2), createChunk(1, 0, 2), createChunk(2, 0, 2)};

        ChunkViewCoreImpl chunkView = new ChunkViewCoreImpl(chunks,
                Region3i.createFromCenterExtents(new Vector3i(1, 0, 1), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1)
                , airBlock);
        assertEquals(new Vector3i(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z),
                chunkView.toWorldPos(Vector3i.zero()));
    }

    private Chunk createChunk(int x, int y, int z) {
        return new ChunkImpl(new Vector3i(x, y, z), blockManager, extraDataManager);
    }
}
