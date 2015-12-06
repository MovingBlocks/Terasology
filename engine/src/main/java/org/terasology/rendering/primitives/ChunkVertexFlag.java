/*
 * Copyright 2013 MovingBlocks
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
 */
public enum ChunkVertexFlag {
    NORMAL(0, "BLOCK_HINT_NORMAL"),
    WATER(1, "BLOCK_HINT_WATER"),
    WATER_SURFACE(2, "BLOCK_HINT_WATER_SURFACE"),
    LAVA(3, "BLOCK_HINT_LAVA"),
    COLOR_MASK(4, "BLOCK_HINT_GRASS"),
    WAVING(5, "BLOCK_HINT_WAVING"),
    WAVING_BLOCK(6, "BLOCK_HINT_WAVING_BLOCK");

    private int value;
    private String defineName;

    private ChunkVertexFlag(int value, String defineName) {
        this.value = value;
        this.defineName = defineName;
    }

    public int getValue() {
        return value;
    }

    public String getDefineName() {
        return defineName;
    }
}
