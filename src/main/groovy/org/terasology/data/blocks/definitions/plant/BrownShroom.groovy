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

package org.terasology.data.blocks.definitions.plant

import org.terasology.world.block.Block.BLOCK_FORM

/**
 * A brown mushroom. It isn't magic yet
 */
block {
    version = 1
    shape = "Billboard"
    blockform = BLOCK_FORM.BILLBOARD

    translucent = true
    transparent = true
    penetrable = true

    hardness = 1
}