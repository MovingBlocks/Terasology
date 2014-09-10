/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.logic.console.Command;
import org.terasology.world.chunks.ChunkProvider;

/**
 * @author Immortius
 */
public class WorldCommands extends BaseComponentSystem {

    private ChunkProvider chunkProvider;

    public WorldCommands(ChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    @Command(shortDescription = "Purges all generated chunks which triggers re-generation")
    public void purgeWorld() {
        chunkProvider.purgeChunks();
    }
}
