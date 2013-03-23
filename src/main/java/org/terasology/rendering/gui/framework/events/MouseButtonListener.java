/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

/**
 * Notified on click events.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public interface MouseButtonListener {

    /**
     * Mouse up event. Will be called if mouse was released anywhere in the UI.
     *
     * @param element   The element of the event.
     * @param button    The button. Left = 0, Right = 1, Middle = 2.
     * @param intersect True if the current mouse position intersects with the element.
     */
    public void up(UIDisplayElement element, int button, boolean intersect);

    /**
     * Mouse down event. Will be called if mouse was pressed anywhere in the UI.
     *
     * @param element   The element of the event.
     * @param button    The button. Left = 0, Right = 1, Middle = 2.
     * @param intersect True if the current mouse position intersects with the element.
     */
    public void down(UIDisplayElement element, int button, boolean intersect);

    /**
     * Mouse wheel moved event. Will be called if mouse wheel was moved anywhere in the UI.
     *
     * @param element   The element of the event.
     * @param wheel     The value of how much the mouse wheel moved. wheel < 0 means down and wheel > 0 means up.
     * @param intersect True if the current mouse position intersects with the element.
     */
    public void wheel(UIDisplayElement element, int wheel, boolean intersect);

}
