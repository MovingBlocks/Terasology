// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui;

import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.rendering.nui.events.NUICharEvent;
import org.terasology.rendering.nui.events.NUIKeyEvent;

/**
 */
public abstract class CoreLayout<T extends LayoutHint> extends AbstractWidget implements UILayout<T> {

    public CoreLayout() {
    }

    public CoreLayout(String id) {
        super(id);
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        return false;
    }

    @Override
    public boolean onCharEvent(NUICharEvent nuiEvent) {
        return false;
    }

    @Override
    public void onBindEvent(BindButtonEvent event) {
    }
}
