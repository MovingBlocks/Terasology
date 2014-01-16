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
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.style.Style;

/**
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class StackLayout implements Layout {

    private UIDisplayElement top;
    private UIDisplayContainer container;

    @Override
    public void layout(UIDisplayContainer newContainer, boolean fitSize) {
        this.container = newContainer;

        for (UIDisplayElement element : newContainer.getDisplayElements()) {
            if (!(element instanceof Style)) {
                element.setSize("100%", "100%");
                if (element == top) {
                    element.setVisible(true);
                } else {
                    element.setVisible(false);
                }
            }
        }
    }

    @Override
    public void render() {

    }

    public void setTop(UIDisplayElement element) {
        if (container != null && container.getDisplayElements().contains(element)) {

            for (UIDisplayElement e : container.getDisplayElements()) {
                if (!(e instanceof Style)) {
                    e.setVisible(false);
                }
            }

            container.orderDisplayElementTop(element);
            element.setVisible(true);

            top = element;
        }
    }
}
