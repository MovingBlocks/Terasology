// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.Color;
import org.terasology.nui.HorizontalAlign;

public interface ParagraphRenderStyle extends TextRenderStyle {
     enum FloatStyle {
        LEFT, RIGHT, NONE
    }

     enum ClearStyle {
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
