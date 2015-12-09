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
package org.terasology.world.chunks;

import org.terasology.math.AABB;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.primitives.ChunkMesh;

/**
 */
@API
public interface RenderableChunk extends LitChunk {

    boolean isDirty();

    void setDirty(boolean dirty);

    AABB getAABB();

    void setMesh(ChunkMesh newMesh);

    void setPendingMesh(ChunkMesh newPendingMesh);

    void setAnimated(boolean animated);

    boolean isAnimated();

    boolean hasMesh();

    boolean hasPendingMesh();

    ChunkMesh getMesh();

    ChunkMesh getPendingMesh();

    void disposeMesh();

    void setAdjacentChunksReady(boolean b);

    boolean areAdjacentChunksReady();

    boolean isReady();
}
