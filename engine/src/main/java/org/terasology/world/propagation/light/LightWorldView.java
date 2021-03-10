// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation.light;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.LitChunk;
import org.terasology.engine.world.propagation.AbstractFullWorldView;

/**
 * Basic world view that provides access to the standard lighting in the world.
 * Simply delegates to getting the value from the provided chunk
 */
public class LightWorldView extends AbstractFullWorldView {

    public LightWorldView(ChunkProvider chunkProvider) {
        super(chunkProvider);
    }

    @Override
    protected byte getValueAt(LitChunk chunk, Vector3ic pos) {
        return chunk.getLight(pos);
    }

    @Override
    protected void setValueAt(LitChunk chunk, Vector3ic pos, byte value) {
        chunk.setLight(pos, value);
    }
}
