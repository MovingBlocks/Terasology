// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation.light;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.LitChunk;
import org.terasology.engine.world.propagation.AbstractFullWorldView;

/**
 * Gets the sunlight from the chunk.
 * <p>
 * Simply delegates to the provided chunk for values
 */
public class SunlightWorldView extends AbstractFullWorldView {

    public SunlightWorldView(ChunkProvider chunkProvider) {
        super(chunkProvider);
    }

    @Override
    protected byte getValueAt(LitChunk chunk, Vector3ic pos) {
        return chunk.getSunlight(pos);
    }

    @Override
    protected void setValueAt(LitChunk chunk, Vector3ic pos, byte value) {
        chunk.setSunlight(pos, value);
    }

}
