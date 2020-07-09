// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui;

import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.events.NUICharEvent;
import org.terasology.rendering.nui.events.NUIKeyEvent;

import java.util.Collections;
import java.util.Iterator;

/**
 */
public abstract class CoreWidget extends AbstractWidget {

    public CoreWidget() {
    }

    public CoreWidget(String id) {
        super(id);
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return Collections.emptyIterator();
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

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
