// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.chunks.ChunkProvider;

/**
 *
 */
public interface RenderableWorld {

    void onChunkLoaded(Vector3i chunkPosition);

    void onChunkUnloaded(Vector3i chunkPosition);

    boolean pregenerateChunks();

    void update();

    boolean updateChunksInProximity(Region3i renderableRegion);

    boolean updateChunksInProximity(ViewDistance viewDistance);

    void generateVBOs();

    int queueVisibleChunks(boolean isFirstRenderingStageForCurrentFrame);

    void dispose();

    RenderQueuesHelper getRenderQueues();

    String getMetrics();

    ChunkProvider getChunkProvider();

    void setShadowMapCamera(Camera camera);
}
