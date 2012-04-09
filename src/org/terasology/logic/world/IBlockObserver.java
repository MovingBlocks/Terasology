/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.world;

import org.terasology.model.structures.BlockPosition;

/**
 * Block observers are notified if a block in the world changes.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public interface IBlockObserver {

    // TODO: Send through source object hint, maybe original + new block type
    public void blockPlaced(Chunk chunk, BlockPosition pos);

    public void blockRemoved(Chunk chunk, BlockPosition pos);
}
