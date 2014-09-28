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
package org.terasology.world;

import org.terasology.world.block.BlockSounds;

/**
 * Serves as a registry for block sound behaviours.
 */
public interface BlockSoundsRegistry {

    /**
     * Retrieve the block sounds with the given URI.
     *
     * @param uri The URI of the block sounds.
     * @return The desired block sounds or null if no such block sounds exist.
     */
    BlockSounds getBlockSounds(String uri);

    /**
     * Gets the default block sounds for blocks that specify no other behaviour.
     * @return Never null.
     */
    BlockSounds getDefaultBlockSounds();

}
