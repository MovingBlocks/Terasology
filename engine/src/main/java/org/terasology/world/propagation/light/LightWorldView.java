/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.propagation.light;

import org.terasology.math.geom.Vector3i;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.propagation.AbstractFullWorldView;

/**
 * Basic world view that provides access to the standard lighting in the world.
 * Simply delegates to getting the value from the provided chunk
 */
public class LightWorldView extends AbstractFullWorldView {

    public LightWorldView(ChunkProvider chunkProvider) {
        super(chunkProvider);
    }

    @Override
    protected byte getValueAt(LitChunk chunk, Vector3i pos) {
        return chunk.getLight(pos);
    }

    @Override
    protected void setValueAt(LitChunk chunk, Vector3i pos, byte value) {
        chunk.setLight(pos, value);
    }
}
