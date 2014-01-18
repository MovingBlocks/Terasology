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

import org.terasology.input.MouseInput;
import org.terasology.math.Vector2i;

/**
 * @author Immortius
 */
public interface InteractionListener {

    void setFocusManager(FocusManager focusManager);

    /**
     * Called every frame the mouse is over the interaction region
     *
     * @param pos            The relative position of the mouse
     * @param topMostElement Whether this element is the top-most element the mouse is over
     */
    void onMouseOver(Vector2i pos, boolean topMostElement);

    /**
     * Called if the mouse ceases to be over the interaction region
     */
    void onMouseLeave();

    /**
     * Called when the mouse is clicked over an interaction region associated with this listener
     *
     * @param button The mouse button that was clicked
     * @param pos    The relative position of the mouse
     * @return Whether the mouse input should be consumed, and thus not propagated to other interaction regions
     */
    boolean onMouseClick(MouseInput button, Vector2i pos);

    /**
     * Called when the mouse is double-clicked over an interaction region associated with this listener.
     * Double clicks occur if the same mouse button is clicked twice with minimal movement and over the same region in a short period.
     *
     * @param button The mouse button that was double clicked
     * @param pos    The relative position of the mouse
     * @return Whether the input should be consumed, and thus not propagated to other interaction regions
     */
    boolean onMouseDoubleClick(MouseInput button, Vector2i pos);

    /**
     * Called when the mouse is moved after clicking on the interaction region
     *
     * @param pos The relative position of the mouse
     */
    void onMouseDrag(Vector2i pos);

    /**
     * Called when the mouse is wheeled while over the interaction region
     *
     * @param wheelTurns
     * @param pos
     * @return Whether the mouse input should be consumed, and thus not propagated to other interaction regions
     */
    boolean onMouseWheel(int wheelTurns, Vector2i pos);

    /**
     * Called when the mouse is released after clicking on the interaction region
     *
     * @param button The mouse button that was clicked
     * @param pos    The relative position of the mouse
     */
    void onMouseRelease(MouseInput button, Vector2i pos);

    /**
     * @return True if the mouse was over the interaction region last frame
     */
    boolean isMouseOver();

}
