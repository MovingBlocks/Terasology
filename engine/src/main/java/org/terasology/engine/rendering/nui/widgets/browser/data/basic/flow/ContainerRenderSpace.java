// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow;

import org.joml.Rectanglei;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

public interface ContainerRenderSpace {
    int getContainerWidth();

    int getNextWidthChange(int y);

    Rectanglei addLeftFloat(int y, int width, int height);

    Rectanglei addRightFloat(int y, int width, int height);

    int getNextClearYPosition(ParagraphRenderStyle.ClearStyle clearStyle);

    int getWidthForVerticalPosition(int y);

    int getAdvanceForVerticalPosition(int y);
}
