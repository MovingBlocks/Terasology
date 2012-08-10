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

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkStore;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public class NullChunkStore implements ChunkStore {

    @Override
    public Chunk get(Vector3i position) {
        return null;
    }

    @Override
    public float size() {
        return 0;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void put(Chunk c) {
    }

    @Override
    public boolean contains(Vector3i position) {
        return false;
    }
}
