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

import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
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
    public void onBindEvent(BindButtonEvent event) {
    }
}
