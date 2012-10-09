/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.input;

import org.lwjgl.input.Keyboard;

/**
 * @author Immortius
 */
public class Input {
    private InputType type;
    private int id;

    public Input(InputType type, int id) {
        this.type = type;
        this.id = id;
    }

    public InputType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        switch (type) {
            case KEY:
                return Keyboard.getKeyName(id);
            case MOUSE_BUTTON:
            case MOUSE_WHEEL:
                return MouseInput.getInputFor(type, id).toString();
        }
        return "";
    }
}
