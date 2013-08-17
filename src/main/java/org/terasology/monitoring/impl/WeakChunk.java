/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.monitoring.impl;

import com.google.common.base.Preconditions;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

import java.lang.ref.WeakReference;

public class WeakChunk {

    protected final Vector3i position;
    protected final WeakReference<Chunk> ref;

    public WeakChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        this.position = chunk.getPos();
        this.ref = new WeakReference<Chunk>(chunk);
    }

    public final Vector3i getPos() {
        return new Vector3i(position);
    }

    public final Chunk getChunk() {
        return ref.get();
    }
}
