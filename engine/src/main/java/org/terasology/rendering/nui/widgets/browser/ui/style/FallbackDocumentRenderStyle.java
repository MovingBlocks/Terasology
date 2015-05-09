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
    public Integer getDocumentIndentTop() {
        Integer documentIndentTop = style.getDocumentIndentTop();
        if (documentIndentTop == null) {
            documentIndentTop = fallback.getDocumentIndentTop();
        }
        return documentIndentTop;
    }

    @Override
    public Integer getDocumentIndentBottom() {
        Integer documentIndentBottom = style.getDocumentIndentBottom();
        if (documentIndentBottom == null) {
            documentIndentBottom = fallback.getDocumentIndentBottom();
        }
        return documentIndentBottom;
    }

    @Override
    public Integer getDocumentIndentLeft() {
        Integer documentIndentLeft = style.getDocumentIndentLeft();
        if (documentIndentLeft == null) {
            documentIndentLeft = fallback.getDocumentIndentLeft();
        }
        return documentIndentLeft;
    }

    @Override
    public Integer getDocumentIndentRight() {
        Integer documentIndentRight = style.getDocumentIndentRight();
        if (documentIndentRight == null) {
            documentIndentRight = fallback.getDocumentIndentRight();
        }
        return documentIndentRight;
    }
}
