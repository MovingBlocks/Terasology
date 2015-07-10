/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.widgets.browser.ui.style;

import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;

public interface ParagraphRenderStyle extends TextRenderStyle {
    public enum FloatStyle {
        LEFT, RIGHT, NONE
    }

    public enum ClearStyle {
        LEFT, RIGHT, BOTH, NONE
    }

    default ContainerInteger getParagraphMarginTop() {
        return null;
    }

    default ContainerInteger getParagraphMarginBottom() {
        return null;
    }

    default ContainerInteger getParagraphMarginLeft() {
        return null;
    }

    default ContainerInteger getParagraphMarginRight() {
        return null;
    }

    default ContainerInteger getParagraphPaddingTop() {
        return null;
    }

    default ContainerInteger getParagraphPaddingBottom() {
        return null;
    }

    default ContainerInteger getParagraphPaddingLeft() {
        return null;
    }

    default ContainerInteger getParagraphPaddingRight() {
        return null;
    }

    default Color getParagraphBackground() {
        return null;
    }

    default ContainerInteger getParagraphMinimumWidth() {
        return null;
    }

    default HorizontalAlign getHorizontalAlignment() {
        return null;
    }

    default FloatStyle getFloatStyle() {
        return null;
    }

    default ClearStyle getClearStyle() {
        return null;
    }
}
