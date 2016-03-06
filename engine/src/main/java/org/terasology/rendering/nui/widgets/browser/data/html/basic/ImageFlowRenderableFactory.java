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
package org.terasology.rendering.nui.widgets.browser.data.html.basic;

import org.terasology.utilities.Assets;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ImageFlowRenderable;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLParseException;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLUtils;
import org.xml.sax.Attributes;

import java.util.Collection;
import java.util.Collections;

public class ImageFlowRenderableFactory implements FlowRenderableFactory {
    @Override
    public Collection<FlowRenderable> create(Attributes attributes, Font font, Color color, String hyperlink) {
        String src = HTMLUtils.findAttribute(attributes, "src");
        if (src == null) {
            throw new HTMLParseException("Expected src attribute");
        }

        Integer width = null;
        Integer height = null;

        String widthStr = HTMLUtils.findAttribute(attributes, "width");
        if (widthStr != null) {
            width = Integer.parseInt(widthStr);
        }
        String heightStr = HTMLUtils.findAttribute(attributes, "height");
        if (heightStr != null) {
            height = Integer.parseInt(heightStr);
        }

        return Collections.singleton(new ImageFlowRenderable(Assets.getTextureRegion(src).get(), width, height, hyperlink));
    }
}
