/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.events;

import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;
import org.terasology.math.geom.Vector2i;

/**
 * See {@link NUIMouseEvent}
 */
public final class NUIMouseOverEvent extends NUIMouseEvent {
    private boolean topMostElement;

    public NUIMouseOverEvent(MouseDevice mouse, KeyboardDevice keyboard, Vector2i relativeMousePosition,
                             boolean topMostElement) {
        super(mouse, keyboard, relativeMousePosition);
        this.topMostElement = topMostElement;
    }

    public boolean isTopMostElement() {
        return topMostElement;
    }
}
