/*
 * Copyright 2013 Moving Blocks
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
 * Notified on mouse events.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public interface MouseMoveListener {
    /**
     * Hover lifecycleEvents. Will be called if the mouse is over the element and moves. (Still mouse won't cause any events.)
     *
     * @param element The element of the lifecycleEvents.
     */
    public void hover(UIDisplayElement element);

    /**
     * Enter lifecycleEvents. Will be called if the mouse enters the element.
     *
     * @param element The element of the lifecycleEvents.
     */
    public void enter(UIDisplayElement element);

    /**
     * Leave lifecycleEvents. Will be called if the mouse leaves the element.
     *
     * @param element The element of the lifecycleEvents.
     */
    public void leave(UIDisplayElement element);

    /**
     * Move lifecycleEvents. Will be called if the mouse moves anywhere in the UI. (Still mouse won't cause any events.)
     *
     * @param element The element of the lifecycleEvents.
     */
    public void move(UIDisplayElement element);
}
