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
package org.terasology.rendering.nui.layout;

import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.LayoutHint;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class ArbitraryLayoutHint implements LayoutHint {

    private LayoutMode mode;
    private Rect2f region = Rect2f.EMPTY;
    private Vector2i size = new Vector2i();
    private Vector2f center = new Vector2f();

    public ArbitraryLayoutHint() {
    }

    public LayoutMode getMode() {
        return mode;
    }

    public void setMode(LayoutMode mode) {
        this.mode = mode;
    }

    public Rect2f getRegion() {
        return region;
    }

    public void setRegion(Rect2f region) {
        this.region = region;
    }

    public Vector2i getSize() {
        return size;
    }

    public void setSize(Vector2i size) {
        this.size.set(size);
    }

    public Vector2f getCenter() {
        return center;
    }

    public void setCenter(Vector2f center) {
        this.center.set(center);
    }

    public enum LayoutMode {
        FIXED,
        FILL
    }
}
