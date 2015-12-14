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
package org.terasology.rendering.assets.atlas;

import org.terasology.math.geom.Vector2i;

/**
 */
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
