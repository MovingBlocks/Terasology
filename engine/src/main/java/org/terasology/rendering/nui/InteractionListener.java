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
public interface InteractionListener {

    void setFocusManager(FocusManager focusManager);

    /**
     * Called every frame the mouse is over the interaction region
     */
    void onMouseOver(NUIMouseOverEvent event);

    /**
     * Called if the mouse ceases to be over the interaction region
     */
    void onMouseLeave();

    /**
     * Called when the mouse is clicked over an interaction region associated with this listener
     *
     * @return Whether the mouse input should be consumed, and thus not propagated to other interaction regions
     */
    boolean onMouseClick(NUIMouseClickEvent event);

    /**
     * Called when the mouse is double-clicked over an interaction region associated with this listener.
     * Double clicks occur if the same mouse button is clicked twice with minimal movement and over the same region in a short period.
     *
     * @return Whether the input should be consumed, and thus not propagated to other interaction regions
     */
    boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event);

    /**
     * Called when the mouse is moved after clicking on the interaction region
     */
    void onMouseDrag(NUIMouseDragEvent event);

    /**
     * Called when the mouse is wheeled while over the interaction region
     *
     * @return Whether the mouse input should be consumed, and thus not propagated to other interaction regions
     */
    boolean onMouseWheel(NUIMouseWheelEvent event);

    /**
     * Called when the mouse is released after clicking on the interaction region
     */
    void onMouseRelease(NUIMouseReleaseEvent event);

    /**
     * @return True if the mouse was over the interaction region last frame
     */
    boolean isMouseOver();

}
