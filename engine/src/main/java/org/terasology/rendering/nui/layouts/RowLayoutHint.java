/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layouts;

import org.terasology.rendering.nui.LayoutHint;

/**
 * @author Immortius
 */
public class RowLayoutHint implements LayoutHint {
    private float relativeWidth;
    private boolean useContentWidth;

    public RowLayoutHint() {
    }

    public RowLayoutHint(float relativeWidth) {
        this.relativeWidth = relativeWidth;
    }

    public float getRelativeWidth() {
        return relativeWidth;
    }

    public void setRelativeWidth(float relativeWidth) {
        this.relativeWidth = relativeWidth;
    }

    public boolean isUseContentWidth() {
        return useContentWidth;
    }

    public void setUseContentWidth(boolean useContentWidth) {
        this.useContentWidth = useContentWidth;
    }
}
