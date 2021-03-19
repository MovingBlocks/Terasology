// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.hud;

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
