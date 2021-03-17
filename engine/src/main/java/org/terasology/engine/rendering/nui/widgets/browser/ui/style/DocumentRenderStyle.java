// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.Color;

public interface DocumentRenderStyle extends ParagraphRenderStyle {
    default ContainerInteger getDocumentMarginTop() {
        return null;
    }

    default ContainerInteger getDocumentMarginBottom() {
        return null;
    }

    default ContainerInteger getDocumentMarginLeft() {
        return null;
    }

    default ContainerInteger getDocumentMarginRight() {
        return null;
    }

    default Color getBackgroundColor() {
        return null;
    }
}
