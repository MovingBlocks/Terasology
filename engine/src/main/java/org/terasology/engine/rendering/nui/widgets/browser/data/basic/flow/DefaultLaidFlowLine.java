// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow;

import java.util.Collections;
import java.util.List;

public class DefaultLaidFlowLine<T extends FlowRenderable> implements LaidFlowLine<T> {
    private final int width;
    private final int height;
    private final List<T> renderables;

    public DefaultLaidFlowLine(int width, int height, List<T> renderables) {
        this.width = width;
        this.height = height;
        this.renderables = renderables;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Iterable<T> getFlowRenderables() {
        return Collections.unmodifiableList(renderables);
    }
}
