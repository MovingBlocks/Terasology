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
package org.terasology.rendering.nui.widgets.browser.data.basic.flow;

import java.util.Collections;
import java.util.List;

public class DefaultLaidFlowLine<T extends FlowRenderable> implements LaidFlowLine<T> {
    private int width;
    private int height;
    private List<T> renderables;

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
