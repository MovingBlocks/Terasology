/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layouts;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 */
public class SingleElementLayout extends CoreLayout<LayoutHint> {
    @LayoutConfig
    private UIWidget content;
    @LayoutConfig
    private boolean updateContent;

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        content = element;
    }

    @Override
    public void removeWidget(UIWidget element) {
        if (Objects.equals(element, content)) {
            content = null;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawWidget(content, Rect2i.createFromMinAndSize(new Vector2i(0, 0), new Vector2i(canvas.size().x, canvas.size().y)));
    }

    @Override
    public void update(float delta) {
        if (updateContent) {
            content.update(delta);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return content != null ? content.getPreferredContentSize(canvas, sizeHint) : new Vector2i();
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return content != null ? content.getMaxContentSize(canvas) : canvas.calculateMaximumSize(content);
    }

    @Override
    public Iterator<UIWidget> iterator() {
        if (content != null) {
            return Collections.singletonList(content).iterator();
        }
        return Collections.emptyIterator();
    }
}
