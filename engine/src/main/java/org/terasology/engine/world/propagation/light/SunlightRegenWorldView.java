// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation.light;

import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.LitChunk;
import org.terasology.engine.world.propagation.AbstractFullWorldView;

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
