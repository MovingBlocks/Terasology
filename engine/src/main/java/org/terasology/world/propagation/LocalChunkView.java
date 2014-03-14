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
package org.terasology.world.propagation;

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;

/**
 * @author Immortius
 */
public class LocalChunkView implements PropagatorWorldView {
    private PropagationRules rules;
    private ChunkImpl[] chunks;

    private Vector3i topLeft = new Vector3i();

    public LocalChunkView(ChunkImpl[] chunks, PropagationRules rules) {
        this.chunks = chunks;
        this.rules = rules;
        topLeft.set(chunks[13].getPos().x - 1, chunks[13].getPos().y - 1, chunks[13].getPos().z - 1);

    }

    private int chunkIndexOf(Vector3i blockPos) {
        return TeraMath.calcChunkPosX(blockPos.x, ChunkConstants.POWER_X) - topLeft.x
                + 3 * (TeraMath.calcChunkPosY(blockPos.y, ChunkConstants.POWER_Y) - topLeft.y
                + 3 * (TeraMath.calcChunkPosZ(blockPos.z, ChunkConstants.POWER_Z) - topLeft.z));
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        return rules.getValue(chunks[chunkIndexOf(pos)], TeraMath.calcBlockPos(pos));
    }

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        rules.setValue(chunks[chunkIndexOf(pos)], TeraMath.calcBlockPos(pos), value);
    }

    @Override
    public Block getBlockAt(Vector3i pos) {
        return chunks[chunkIndexOf(pos)].getBlock(TeraMath.calcBlockPos(pos));
    }
}
