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

import org.terasology.world.chunks.RenderableChunk;

import java.util.PriorityQueue;

/**
 * Created by manu on 25.12.2014.
 */
public class RenderQueuesHelper {
    public final PriorityQueue<RenderableChunk> chunksOpaque;
    public final PriorityQueue<RenderableChunk> chunksOpaqueShadow;
    public final PriorityQueue<RenderableChunk> chunksOpaqueReflection;
    public final PriorityQueue<RenderableChunk> chunksAlphaReject;
    public final PriorityQueue<RenderableChunk> chunksAlphaBlend;

    RenderQueuesHelper(PriorityQueue<RenderableChunk> chunksOpaque,
                       PriorityQueue<RenderableChunk> chunksOpaqueShadow,
                       PriorityQueue<RenderableChunk> chunksOpaqueReflection,
                       PriorityQueue<RenderableChunk> chunksAlphaReject,
                       PriorityQueue<RenderableChunk> chunksAlphaBlend) {

        this.chunksOpaque = chunksOpaque;
        this.chunksOpaqueShadow = chunksOpaqueShadow;
        this.chunksOpaqueReflection = chunksOpaqueReflection;
        this.chunksAlphaReject = chunksAlphaReject;
        this.chunksAlphaBlend = chunksAlphaBlend;
    }
}
