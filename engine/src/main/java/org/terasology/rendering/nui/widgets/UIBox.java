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
package org.terasology.rendering.nui.widgets;

import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;

import java.util.Collections;
import java.util.Iterator;

/**
 * A simple element that just renders a box-style background.
 */
public class UIBox extends CoreWidget {

    @LayoutConfig
    private UIWidget content;

    @LayoutConfig
    private boolean updateContent = true;

    @Override
    public void onDraw(Canvas canvas) {
        if (content != null) {
            canvas.drawWidget(content);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (content != null) {
            return canvas.calculateRestrictedSize(content, sizeHint);
        }
        return Vector2i.zero();
    }

    @Override
    public void update(float delta) {
        if (updateContent) {
            super.update(delta);
        }
    }

    /**
     * @return The UIWidget inside the box.
     */
    public UIWidget getContent() {
        return content;
    }

    /**
     * @param content The UIWidget to set as the contents of the box.
     */
    public void setContent(UIWidget content) {
        this.content = content;
        content.setEnabled(isEnabled());
    }

    /**
     * @return A Boolean indicating if the content is updated or not.
     */
    public boolean getUpdateContent() {
        return updateContent;
    }

    /**
     * @param value A Boolean to indicate if the contents should be updated
     */
    public void setUpdateContent(Boolean value) {
        updateContent = value;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        if (content != null) {
            return Collections.singletonList(content).iterator();
        }
        return Collections.emptyIterator();
    }
}
