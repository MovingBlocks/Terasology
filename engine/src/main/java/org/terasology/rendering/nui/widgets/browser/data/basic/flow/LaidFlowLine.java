// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow;

public interface LaidFlowLine<T extends FlowRenderable> {
    int getWidth();

    int getHeight();

    Iterable<T> getFlowRenderables();
}
