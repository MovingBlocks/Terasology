// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.Color;
import org.terasology.nui.asset.font.Font;

public class FallbackTextRenderStyle implements TextRenderStyle {
    private TextRenderStyle style;
    private TextRenderStyle fallback;

    public FallbackTextRenderStyle(TextRenderStyle style, TextRenderStyle fallback) {
        this.style = style;
        this.fallback = fallback;
    }

    @Override
    public Font getFont(boolean hyperlink) {
        Font font = style.getFont(hyperlink);
        if (font == null) {
            font = fallback.getFont(hyperlink);
        }
        return font;
    }

    @Override
    public Color getColor(boolean hyperlink) {
        Color color = style.getColor(hyperlink);
        if (color == null) {
            color = fallback.getColor(hyperlink);
        }
        return color;
    }
}
