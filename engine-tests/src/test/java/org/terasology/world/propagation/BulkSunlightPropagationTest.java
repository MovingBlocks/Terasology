// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BulkSunlightPropagationTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;
    private Block air;
    private Block solid;
    private SunlightPropagationRules lightRules;
    private SunlightRegenPropagationRules regenRules;

    private StubPropagatorWorldView regenWorldView;
    private StubPropagatorWorldView lightWorldView;

    private BatchPropagator sunlightPropagator;
    private SunlightRegenBatchPropagator propagator;


    @BeforeEach
    public void setup() throws Exception {

        regenRules = new SunlightRegenPropagationRules();
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager, true);
        CoreRegistry.put(BlockManager.class, blockManager);

        BlockFamilyDefinitionData solidData = new BlockFamilyDefinitionData();
        solidData.getBaseSection().setDisplayName("Stone");
        solidData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        solidData.getBaseSection().setTranslucent(false);
        solidData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:stone"), solidData, BlockFamilyDefinition.class);
        solid = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:stone")));

        air = blockManager.getBlock(BlockManager.AIR_ID);

        Map<Vector3ic, Block> blockData = Maps.newHashMap();
        regenWorldView = new StubPropagatorWorldView(Chunks.CHUNK_REGION, air, blockData);
        lightWorldView = new StubPropagatorWorldView(Chunks.CHUNK_REGION, air, blockData);

        lightRules = new SunlightPropagationRules(regenWorldView);
        sunlightPropagator = new StandardBatchPropagator(lightRules, lightWorldView);
        propagator = new SunlightRegenBatchPropagator(regenRules, regenWorldView, sunlightPropagator, lightWorldView);


    }

    @Test
    public void testAllowSunlightVertical() {
        for (Vector3ic pos : new BlockRegion(0, 16, 0).union(Chunks.SIZE_X - 1, Chunks.SIZE_Y - 1, Chunks.SIZE_Z - 1)) {
            regenWorldView.setValueAt(pos, Chunks.MAX_SUNLIGHT_REGEN);
            lightWorldView.setValueAt(pos, Chunks.MAX_SUNLIGHT);
        }
        for (Vector3ic pos : new BlockRegion(0, 15, 0).union(Chunks.SIZE_X - 1, 15, Chunks.SIZE_Z - 1)) {
            regenWorldView.setBlockAt(new Vector3i(pos), solid);
        }
        for (Vector3ic pos : new BlockRegion(0, 0, 0).union(Chunks.SIZE_X - 1, 14, Chunks.SIZE_Z - 1)) {
            regenWorldView.setValueAt(pos, (byte) (14 - pos.y()));
        }

        regenWorldView.setBlockAt(new Vector3i(16, 15, 16), air);
        propagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));
        sunlightPropagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));

        for (int y = 0; y < 16; y++) {
            assertEquals(Chunks.MAX_SUNLIGHT_REGEN, regenWorldView.getValueAt(new Vector3i(16, y, 16)), "Incorrect value at " + y);
            assertEquals(Chunks.MAX_SUNLIGHT, lightWorldView.getValueAt(new Vector3i(16, y, 16)));
        }
        for (int y = 0; y < 15; y++) {
            assertEquals(Chunks.MAX_SUNLIGHT - 1, lightWorldView.getValueAt(new Vector3i(15, y, 16)));
        }
    }

    @Test
    public void testStopSunlightVertical() {
        for (Vector3ic pos : new BlockRegion(0, 16, 0).union(Chunks.SIZE_X - 1, Chunks.SIZE_Y - 1, Chunks.SIZE_Z - 1)) {
            regenWorldView.setValueAt(pos, Chunks.MAX_SUNLIGHT_REGEN);
            lightWorldView.setValueAt(pos, Chunks.MAX_SUNLIGHT);
        }
        for (Vector3ic pos : new BlockRegion(0, 15, 0).union(Chunks.SIZE_X - 1, 15, Chunks.SIZE_Z - 1)) {
            regenWorldView.setBlockAt(new Vector3i(pos), solid);
        }
        for (Vector3ic pos : new BlockRegion(0, 0, 0).union(Chunks.SIZE_X - 1, 14, Chunks.SIZE_Z - 1)) {
            regenWorldView.setValueAt(pos, (byte) (14 - pos.y()));
        }

        regenWorldView.setBlockAt(new Vector3i(16, 15, 16), air);
        propagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));
        sunlightPropagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));

        regenWorldView.setBlockAt(new Vector3i(16, 15, 16), solid);
        propagator.process(new BlockChange(new Vector3i(16, 15, 16), air, solid));
        sunlightPropagator.process(new BlockChange(new Vector3i(16, 15, 16), air, solid));

        for (Vector3ic pos : new BlockRegion(0, 0, 0).union(Chunks.SIZE_X - 1, 15, Chunks.SIZE_Z - 1)) {
            assertEquals(Math.max(0, 14 - pos.y()), regenWorldView.getValueAt(pos), "Incorrect value at " + pos);
            assertEquals(0, lightWorldView.getValueAt(pos), "Incorrect value at " + pos);
        }
    }

}
