/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.math.Vector2i;

/**
 * @author Immortius
 */
public abstract class BaseInteractionListener implements InteractionListener {

    private boolean mouseOver;

    @Override
    public void setMouseOver(Vector2i pos, boolean value) {
        this.mouseOver = value;
    }

    @Override
    public boolean onMouseClick(int button, Vector2i pos) {
        return false;
    }

    @Override
    public boolean onMouseRelease(int button, Vector2i pos) {
        return false;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }
}
