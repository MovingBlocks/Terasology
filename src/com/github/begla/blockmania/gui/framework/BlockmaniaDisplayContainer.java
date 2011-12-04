/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.gui.framework;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * Composition of multiple display elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class BlockmaniaDisplayContainer extends BlockmaniaDisplayElement {

    ArrayList<BlockmaniaDisplayElement> _displayElements = new ArrayList<BlockmaniaDisplayElement>();

    public void render() {
        glPushMatrix();
        glTranslatef(getPosition().x, getPosition().y, 0);

        // Render all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).renderElement();
        }

        glPopMatrix();
    }

    public void update() {
        // Update all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).update();
        }
    }

    @Override
    public void processKeyboardInput(int key) {
        // Pass the pressed key to all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).processKeyboardInput(key);
        }
    }

    public void addDisplayElement(BlockmaniaDisplayElement element) {
        _displayElements.add(element);
    }

    public void removeDisplayElement(BlockmaniaDisplayElement element) {
        _displayElements.remove(element);
    }

    public ArrayList<BlockmaniaDisplayElement> getDisplayElements() {
        return _displayElements;
    }
}
