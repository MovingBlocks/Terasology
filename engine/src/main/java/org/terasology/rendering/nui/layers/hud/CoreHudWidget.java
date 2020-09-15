/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.hud;

import org.joml.Vector2i;
import org.terasology.nui.Canvas;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.UIWidget;

import java.util.Arrays;
import java.util.Iterator;

/**
 */
public abstract class CoreHudWidget extends CoreWidget implements ControlWidget {

    @LayoutConfig
    private UIWidget contents;

    private boolean initialised;

    public void setContents(UIWidget contents) {
        this.contents = contents;
    }

    public UIWidget getContents() {
        return contents;
    }

    @Override
    public void onOpened() {
        if (!initialised) {
            initialise();
            initialised = true;
        }
    }

    @Override
    public void onClosed() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (contents != null) {
            canvas.drawWidget(contents, canvas.getRegion());
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return Arrays.asList(contents).iterator();
    }
}
