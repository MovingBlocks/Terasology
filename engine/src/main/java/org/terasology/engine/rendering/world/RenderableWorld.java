// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import org.joml.Vector3ic;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.ChunkProvider;

/**
 *
 */
public interface RenderableWorld {

    void onChunkLoaded(Vector3ic chunkPosition);

    void onChunkUnloaded(Vector3ic chunkPosition);

    boolean pregenerateChunks();

    void update();

    boolean updateChunksInProximity(BlockRegion renderableRegion);

    boolean updateChunksInProximity(ViewDistance viewDistance, int chunkLods);

    void generateVBOs();

    int queueVisibleChunks(boolean isFirstRenderingStageForCurrentFrame);

    void dispose();

    RenderQueuesHelper getRenderQueues();

    String getMetrics();

    ChunkProvider getChunkProvider();

    void setShadowMapCamera(Camera camera);
}
