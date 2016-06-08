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
package org.terasology.rendering.world.dag;

import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.WorldRendererImpl;
import org.terasology.utilities.collection.DirectedAcyclicClassGraph;
import org.terasology.world.chunks.RenderableChunk;


public class DAGRendererSystem implements ComponentSystem {

    public DirectedAcyclicClassGraph<RenderNode> nodes = new DirectedAcyclicClassGraph<>();

    public DAGRendererSystem() {

    }


    public void renderChunk(RenderableChunk chunk, ChunkMesh.RenderPhase phase, Camera camera, WorldRendererImpl.ChunkRenderMode mode) {

    }


    public void resetStats() {

    }

    @Override
    public void initialise() {

    }

    @Override
    public void preBegin() {

    }

    @Override
    public void postBegin() {

    }

    @Override
    public void preSave() {

    }

    @Override
    public void postSave() {

    }

    @Override
    public void shutdown() {

    }

    public void addNode(RenderNode node) {
        node.renderer = this;
        nodes.addNode(node);
    }
}
