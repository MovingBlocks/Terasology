// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.atlas;


import org.joml.Vector2i;

public class FreeformDefinition {

    private String name;
    private Vector2i min;
    private Vector2i max;
    private Vector2i size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector2i getMin() {
        return min;
    }

    public void setMin(Vector2i min) {
        this.min = min;
    }

    public Vector2i getMax() {
        return max;
    }

    public void setMax(Vector2i max) {
        this.max = max;
    }

    public Vector2i getSize() {
        return size;
    }

    public void setSize(Vector2i size) {
        this.size = size;
    }
}
