/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.gui.layout;

import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * Interface for layout classes which can be used in the UIComposite container.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public interface Layout {
    /**
     * Arranges the child elements within a UIComposite.
     *
     * @param container The container of the child elements.
     * @param fitSize   True if the layout should set the size of the container.
     */
    void layout(UIDisplayContainer container, boolean fitSize);

    /**
     * Render something in the layout class itself.
     */
    void render();
}
