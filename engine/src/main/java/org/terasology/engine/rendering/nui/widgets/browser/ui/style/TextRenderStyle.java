// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.asset.font.Font;
import org.terasology.nui.Color;

public interface TextRenderStyle {
    default Font getFont(boolean hyperlink) {
        return null;
    }

    default Color getColor(boolean hyperlink) {
        return null;
    }
}
