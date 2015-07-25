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
package org.terasology.rendering.world;

import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.chunks.ChunkProvider;

/**
 * Created by manu on 24.12.2014.
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
}
