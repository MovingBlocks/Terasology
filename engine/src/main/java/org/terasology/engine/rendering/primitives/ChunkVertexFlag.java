// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import org.terasology.context.annotation.API;

@API
public enum ChunkVertexFlag {
    NORMAL(0, "BLOCK_HINT_NORMAL"),
    WATER(1, "BLOCK_HINT_WATER"),
    WATER_SURFACE(2, "BLOCK_HINT_WATER_SURFACE"),
    COLOR_MASK(3, "BLOCK_HINT_GRASS"),
    WAVING(4, "BLOCK_HINT_WAVING"),
    WAVING_BLOCK(5, "BLOCK_HINT_WAVING_BLOCK");

    private int value;
    private String defineName;

    ChunkVertexFlag(int value, String defineName) {
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
