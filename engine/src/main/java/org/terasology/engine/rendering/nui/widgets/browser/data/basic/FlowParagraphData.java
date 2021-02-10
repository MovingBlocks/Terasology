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
package org.terasology.rendering.nui.widgets.browser.data.basic;

import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

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
