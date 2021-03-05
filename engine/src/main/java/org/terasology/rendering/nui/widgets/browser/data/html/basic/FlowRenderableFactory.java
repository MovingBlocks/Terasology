// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.nui.Color;
import org.terasology.nui.asset.font.Font;
import org.xml.sax.Attributes;

import java.util.Collection;

@FunctionalInterface
public interface FlowRenderableFactory {
    Collection<FlowRenderable> create(Attributes attributes, Font font, Color color, String hyperlink);
}
