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
