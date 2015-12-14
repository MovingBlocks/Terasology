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
package org.terasology.rendering.nui;

import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDoubleClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;

/**
 */
public class BaseInteractionListener implements InteractionListener {

    protected FocusManager focusManager;
    private boolean mouseOver;

    @Override
    public void setFocusManager(FocusManager focusManager) {
        this.focusManager = focusManager;
    }

    @Override
    public void onMouseOver(NUIMouseOverEvent event) {
        this.mouseOver = event.isTopMostElement();
    }

    @Override
    public void onMouseLeave() {
        this.mouseOver = false;
    }

    @Override
    public boolean onMouseClick(NUIMouseClickEvent event) {
        return false;
    }

    @Override
    public boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event) {
        return onMouseClick(event);
    }

    @Override
    public void onMouseDrag(NUIMouseDragEvent event) {

    }

    @Override
    public void onMouseRelease(NUIMouseReleaseEvent event) {
    }

    @Override
    public boolean onMouseWheel(NUIMouseWheelEvent event) {
        return false;
    }

    @Override
    public boolean isMouseOver() {
        return mouseOver;
    }
}
