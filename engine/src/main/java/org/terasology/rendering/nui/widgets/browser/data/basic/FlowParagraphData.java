// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic;

import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FlowParagraphData implements ParagraphData {
    private List<FlowRenderable> data = new LinkedList<>();

    private ParagraphRenderStyle paragraphRenderStyle;

    public FlowParagraphData(ParagraphRenderStyle paragraphRenderStyle) {
        this.paragraphRenderStyle = paragraphRenderStyle;
    }

    @Override
    public ParagraphRenderStyle getParagraphRenderStyle() {
        return paragraphRenderStyle;
    }

    @Override
    public ParagraphRenderable getParagraphContents() {
        return new FlowParagraphRenderable(Collections.unmodifiableList(data));
    }

    public void append(FlowRenderable flowRenderable) {
        data.add(flowRenderable);
    }

    public void append(Collection<FlowRenderable> flowRenderable) {
        data.addAll(flowRenderable);
    }
}
