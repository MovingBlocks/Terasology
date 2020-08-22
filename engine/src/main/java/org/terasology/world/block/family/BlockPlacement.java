/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.world.block.family;

import org.joml.Vector3ic;

import org.terasology.world.block.Block;

/**
 * A class representing a placement decision about what block to place and where.
 */
public class BlockPlacement {
    public final Vector3ic position;
    public final Block block;

    public BlockPlacement(Vector3ic position, Block block) {
        this.position = position;
        this.block = block;

        assert(block != null);
    }
}
