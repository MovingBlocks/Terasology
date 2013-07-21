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
package org.terasology.rendering.primitives;

/**
 * @author Immortius
 */
public enum ChunkVertexFlag {
    BLOCK_HINT_NORMAL(0),
    BLOCK_HINT_WATER(1),
    BLOCK_HINT_LAVA(2),
    BLOCK_HINT_COLOR_MASK(3),
    BLOCK_HINT_WAVING(4),
    BLOCK_HINT_WAVING_BLOCK(5);

    private int value;

    private ChunkVertexFlag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
