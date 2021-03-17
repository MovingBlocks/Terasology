// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.Color;

public class FallbackDocumentRenderStyle extends FallbackParagraphRenderStyle implements DocumentRenderStyle {
    private DocumentRenderStyle style;
    private DocumentRenderStyle fallback;

    public FallbackDocumentRenderStyle(DocumentRenderStyle style, DocumentRenderStyle fallback) {
        super(style, fallback);
        this.style = style;
        this.fallback = fallback;
    }

    @Override
    public Color getBackgroundColor() {
        Color backgroundColor = style.getBackgroundColor();
        if (backgroundColor == null) {
            backgroundColor = fallback.getBackgroundColor();
        }
        return backgroundColor;
    }

    @Override
    public ContainerInteger getDocumentMarginTop() {
        ContainerInteger documentIndentTop = style.getDocumentMarginTop();
        if (documentIndentTop == null) {
            documentIndentTop = fallback.getDocumentMarginTop();
        }
        return documentIndentTop;
    }

    @Override
    public ContainerInteger getDocumentMarginBottom() {
        ContainerInteger documentIndentBottom = style.getDocumentMarginBottom();
        if (documentIndentBottom == null) {
            documentIndentBottom = fallback.getDocumentMarginBottom();
        }
        return documentIndentBottom;
    }

    @Override
    public ContainerInteger getDocumentMarginLeft() {
        ContainerInteger documentIndentLeft = style.getDocumentMarginLeft();
        if (documentIndentLeft == null) {
            documentIndentLeft = fallback.getDocumentMarginLeft();
        }
        return documentIndentLeft;
    }

    @Override
    public ContainerInteger getDocumentMarginRight() {
        ContainerInteger documentIndentRight = style.getDocumentMarginRight();
        if (documentIndentRight == null) {
            documentIndentRight = fallback.getDocumentMarginRight();
        }
        return documentIndentRight;
    }
}
