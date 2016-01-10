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

package org.terasology.input;

/**
 * The type of an input
 *
 */
public enum InputType {
    NONE {
        private final transient UnknownInput noneNone = new UnknownInput(this, 0);

        @Override
        public Input getInput(int id) {
            return noneNone;
        }

        @Override
        public Input getInput(String name) {
            return null;
        }
    },
    KEY {
        @Override
        public Input getInput(int id) {
            return Keyboard.Key.find(id);
        }

        @Override
        public Input getInput(String name) {
            return Keyboard.Key.find(name);
        }
    },
    MOUSE_BUTTON {
        @Override
        public Input getInput(int id) {
            return MouseInput.find(this, id);
        }

        @Override
        public Input getInput(String name) {
            return MouseInput.find(name);
        }
    },
    MOUSE_WHEEL {
        @Override
        public Input getInput(int id) {
            return MouseInput.find(this, id);
        }

        @Override
        public Input getInput(String name) {
            return MouseInput.find(name);
        }
    },
    CONTROLLER_BUTTON {
        @Override
        public Input getInput(int id) {
            return ControllerInput.find(this, id);
        }

        @Override
        public Input getInput(String name) {
            return ControllerInput.find(name);
        }
    },
    CONTROLLER_AXIS {
        @Override
        public Input getInput(int id) {
            return ControllerInput.find(this, id);
        }

        @Override
        public Input getInput(String name) {
            return ControllerInput.find(name);
        }
    };

    public abstract Input getInput(int id);
    public abstract Input getInput(String name);

    public static Input parse(String inputName) {
        for (InputType type : values()) {
            Input result = type.getInput(inputName);
            if (result != null) {
                return result;
            }
        }
        return UnknownInput.tryParse(inputName);
    }

}
