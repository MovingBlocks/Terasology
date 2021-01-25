// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.joml.geom.AABBfc;
import org.terasology.math.geom.Vector3f;
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
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.internal.ChunkImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkTest extends TerasologyTestingEnvironment {

    private Chunk chunk;
    private BlockManagerImpl blockManager;
    private Block solid;

    @BeforeEach
    public void setup() throws Exception {
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
        assertEquals(new Vector3f(0, 0, 0), new Vector3f(aabb.minX(), aabb.minY(), aabb.minZ()));
        assertEquals(new Vector3f(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z), new Vector3f(aabb.maxX(), aabb.maxY(), aabb.maxZ()));
    }

}

