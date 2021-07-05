// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.joml.geom.AABBfc;
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
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("TteTest")
public class ChunkTest extends TerasologyTestingEnvironment {

    private Chunk chunk;
    private BlockManagerImpl blockManager;
    private Block solid;

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager);
        CoreRegistry.put(BlockManager.class, blockManager);

        ExtraBlockDataManager extraDataManager = new ExtraBlockDataManager();

        chunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, extraDataManager);

        BlockFamilyDefinitionData solidData = new BlockFamilyDefinitionData();
        solidData.getBaseSection().setDisplayName("Stone");
        solidData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        solidData.getBaseSection().setTranslucent(false);
        solidData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:stone"), solidData, BlockFamilyDefinition.class);
        solid = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:stone")));
    }

    @Test
    public void testChangeBlock() {
        chunk.setBlock(new Vector3i(1, 2, 3), solid);
        assertEquals(solid, chunk.getBlock(new Vector3i(1, 2, 3)));
    }

    @Test
    public void testGetAabb() {
        AABBfc aabb = chunk.getAABB();
        assertEquals(new Vector3f(-.5f, -.5f, -.5f), new Vector3f(aabb.minX(), aabb.minY(), aabb.minZ()));
        assertEquals(new Vector3f(Chunks.SIZE_X - .5f, Chunks.SIZE_Y - .5f, Chunks.SIZE_Z - .5f),
                new Vector3f(aabb.maxX(), aabb.maxY(), aabb.maxZ()));
    }

}

