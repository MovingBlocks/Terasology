// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.unittest.worlds.DummyWorldGenerator;

import static com.google.common.truth.Truth.assertThat;
import static org.terasology.engine.world.block.BlockManager.AIR_ID;
import static org.terasology.engine.world.block.BlockManager.UNLOADED_ID;

@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
class ChunkRegionFutureTest {

    Vector3fc center = new Vector3f(2021, DummyWorldGenerator.SURFACE_HEIGHT, 1117);
    Vector3ic sizeInChunks = new Vector3i(9, 3, 5);

    @In
    WorldProvider world;

    @Test
    void createChunkRegionFuture(EntityManager entityManager, RelevanceSystem relevanceSystem, MainLoop mainLoop) {
        ChunkRegionFuture chunkRegionFuture = ChunkRegionFuture.create(entityManager, relevanceSystem, center, sizeInChunks);

        mainLoop.runUntil(chunkRegionFuture.getFuture());

        Vector3fc someplaceInside = center.add(
                 sizeInChunks.x() * Chunks.SIZE_X / 3f, 0, 0,
                new Vector3f());
        Vector3fc someplaceOutside = center.add(
                0, 0, sizeInChunks.z() * Chunks.SIZE_Z * 3f,
                new Vector3f());

        Block blockAtCenter = world.getBlock(center);
        Block blockInside = world.getBlock(someplaceInside);
        Block blockOutside = world.getBlock(someplaceOutside);

        assertThat(blockAtCenter).isNotNull();
        assertThat(blockAtCenter.getURI()).isEqualTo(AIR_ID);

        assertThat(blockInside).isNotNull();
        assertThat(blockInside.getURI()).isEqualTo(AIR_ID);

        assertThat(blockOutside.getURI()).isEqualTo(UNLOADED_ID);
    }
}
