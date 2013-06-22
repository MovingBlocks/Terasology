/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.components.actions;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.Component;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.BlockGrid;
import org.terasology.world.chunks.MiniatureChunk;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class MiniaturizerComponent implements Component {

    public static final float SCALE = 1f / 32f;

    public transient ChunkMesh chunkMesh;
    public float orientation;
    public Vector3f renderPosition;
    public MiniatureChunk miniatureChunk;
    public BlockGrid blockGrid = new BlockGrid();

    public void reset() {
        if (chunkMesh != null) {
            chunkMesh.dispose();
            chunkMesh = null;
        }

        orientation = 0;
        renderPosition = null;
        miniatureChunk = null;
    }
}
