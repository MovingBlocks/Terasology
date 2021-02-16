// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.Color;
import org.terasology.nui.asset.font.Font;

public interface TextRenderStyle {
    default Font getFont(boolean hyperlink) {
        return null;
    }

    default Color getColor(boolean hyperlink) {
        return null;
    }
}
