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

/**
 * @param <T> the class type of the layout hint
 */
public interface UILayout<T extends LayoutHint> extends UIWidget {

    /**
     * Adds a widget with an optional layout hint
     *
     * @param element the element to add
     * @param hint    A hint as to how the widget should be laid out - may be null (and null values should be handled).
     */
    void addWidget(UIWidget element, T hint);

    /**
     * Removes a widget from the layout.
     * @param element the element to remove
     */
    void removeWidget(UIWidget element);
}
