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
package org.terasology.world.chunks.internal;

import gnu.trove.list.TIntList;
import gnu.trove.map.TShortObjectMap;
import org.terasology.math.Vector3i;
import org.terasology.persistence.ChunkStore;

/**
 * @author Immortius
 */
public class ReadyChunkInfo {
    private Vector3i pos;
    private TShortObjectMap<TIntList> blockPositionMapppings;
    private ChunkStore chunkStore;

    public ReadyChunkInfo(Vector3i pos, TShortObjectMap<TIntList> blockPositionMapppings) {
        this.pos = pos;
        this.blockPositionMapppings = blockPositionMapppings;
    }

    public ReadyChunkInfo(Vector3i pos, TShortObjectMap<TIntList> blockPositionMapppings, ChunkStore chunkStore) {
        this.pos = pos;
        this.blockPositionMapppings = blockPositionMapppings;
        this.chunkStore = chunkStore;
    }

    public Vector3i getPos() {
        return pos;
    }

    public TShortObjectMap<TIntList> getBlockPositionMapppings() {
        return blockPositionMapppings;
    }

    public ChunkStore getChunkStore() {
        return chunkStore;
    }
}
