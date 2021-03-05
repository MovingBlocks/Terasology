// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.ImageFlowRenderable;
import org.terasology.nui.Color;
import org.terasology.nui.asset.font.Font;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLParseException;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLUtils;
import org.terasology.engine.utilities.Assets;
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
