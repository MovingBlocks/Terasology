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
import org.terasology.rendering.nui.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 */
public class UIBox extends CoreWidget {

    @LayoutConfig
    private UIWidget content;

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

    public UIWidget getContent() {
        return content;
    }

    public void setContent(UIWidget content) {
        this.content = content;
        if (content instanceof AbstractWidget) {
            ((AbstractWidget) content).setEnabled(isEnabled());
        }
    }

    @Override
    public Iterator<UIWidget> iterator() {
        if (content != null) {
            return Arrays.asList(content).iterator();
        }
        return Collections.emptyIterator();
    }
}
