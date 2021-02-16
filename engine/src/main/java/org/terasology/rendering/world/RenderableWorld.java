/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.world;

import org.joml.Vector3ic;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.ChunkProvider;

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
