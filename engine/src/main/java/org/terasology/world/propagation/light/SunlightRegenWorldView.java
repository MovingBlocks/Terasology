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
package org.terasology.world.propagation.light;

import org.terasology.math.geom.Vector3i;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.propagation.AbstractFullWorldView;

/**
 * Gets the sunlight regen values from the chunk.
 * <p>
 * Simply delegates to the provided chunk for values
 */
public class SunlightRegenWorldView extends AbstractFullWorldView {

    public SunlightRegenWorldView(ChunkProvider chunkProvider) {
        super(chunkProvider);
    }

    @Override
    protected byte getValueAt(LitChunk chunk, Vector3i pos) {
        return chunk.getSunlightRegen(pos);
    }

    @Override
    protected void setValueAt(LitChunk chunk, Vector3i pos, byte value) {
        chunk.setSunlightRegen(pos, value);
    }
}
