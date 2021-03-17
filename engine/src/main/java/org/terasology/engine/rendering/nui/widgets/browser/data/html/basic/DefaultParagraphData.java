// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

public class DefaultParagraphData implements ParagraphData {
    private ParagraphRenderStyle paragraphRenderStyle;
    private ParagraphRenderable paragraphRenderable;

    public DefaultParagraphData(ParagraphRenderStyle paragraphRenderStyle, ParagraphRenderable paragraphRenderable) {
        this.paragraphRenderStyle = paragraphRenderStyle;
        this.paragraphRenderable = paragraphRenderable;
    }

    @Override
    public ParagraphRenderStyle getParagraphRenderStyle() {
        return paragraphRenderStyle;
    }

    @Override
    public ParagraphRenderable getParagraphContents() {
        return paragraphRenderable;
    }
}
