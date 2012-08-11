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
package org.terasology.world.chunks.store;


import java.util.concurrent.ConcurrentMap;

import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkStore;

import com.google.common.collect.Maps;

public class ChunkStoreUncompressed implements ChunkStore {
    ConcurrentMap<Vector3i, Chunk> map = Maps.newConcurrentMap();
    int _sizeInByte = 0;

    public ChunkStoreUncompressed() {

    }

    public Chunk get(Vector3i id) {
        return map.get(id);
    }

    public void put(Chunk c) {
        map.put(c.getPos(), c);
    }

    @Override
    public boolean contains(Vector3i position) {
        return map.containsKey(position);
    }

    public float size() {
        return 0;
    }

    public void dispose() {
    }
}
