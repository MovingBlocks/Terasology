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

package org.terasology.world.localChunkProvider;

import org.terasology.math.Vector3i;
import org.terasology.world.chunks.provider.ChunkProvider;

/**
 * @author Immortius
 */
public class ShutdownTask implements ChunkTask {

    @Override
    public void enact() {
    }

    @Override
    public boolean isShutdownRequest() {
        return true;
    }

    @Override
    public Vector3i getPosition() {
        return Vector3i.zero();
    }

    @Override
    public ChunkProvider getProvider() {
        return null;
    }
}
