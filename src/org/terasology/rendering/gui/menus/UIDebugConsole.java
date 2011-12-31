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
package org.terasology.rendering.gui.menus;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.terasology.game.Terasology;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * The debug console of Terasology.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class UIDebugConsole extends UIDisplayContainer {

    private final UIText _consoleText;

    private final StringBuffer _consoleInput = new StringBuffer();
    private final ArrayList<String> _ringBuffer = new ArrayList<String>();
    private int _ringBufferPos = -1;

    /**
     * Init. a new Terasology console.
     */
    public UIDebugConsole() {
        _consoleText = new UIText();
        _consoleText.setVisible(true);

        addDisplayElement(_consoleText);

        update();
    }

    /**
     * Processes the given keyboard input.
     *
     * @param key The key
     */
    public void processKeyboardInput(int key) {
        super.processKeyboardInput(key);

        if (!isVisible())
            return;

        if (key == Keyboard.KEY_BACK) {
            int length = _consoleInput.length() - 1;

            if (length < 0) {
                length = 0;
            }
            _consoleInput.setLength(length);
        } else if (key == Keyboard.KEY_RETURN) {
            processConsoleString();
        } else if (key == Keyboard.KEY_UP) {
            rotateRingBuffer(1);
        } else if (key == Keyboard.KEY_DOWN) {
            rotateRingBuffer(-1);
        }

        char c = Keyboard.getEventCharacter();

        if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'Z' + 1 || c == ' ' || c == '_' || c == '.' || c == ',' || c == '!' || c == '-' || c == '(' || c == ')' || c == '"' || c == '\'' || c == ';' || c == '+') {
            _consoleInput.append(c);
        }
    }

    /**
     * Rotates the ring buffer in the given direction.
     *
     * @param dir The direction
     */
    public void rotateRingBuffer(int dir) {
        if (_ringBuffer.isEmpty())
            return;

        _ringBufferPos += dir;

        if (_ringBufferPos < 0)
            _ringBufferPos = _ringBuffer.size() - 1;
        else if (_ringBufferPos > _ringBuffer.size() - 1)
            _ringBufferPos = 0;

        _consoleInput.setLength(0);
        _consoleInput.append(_ringBuffer.get(_ringBufferPos));
    }

    /**
     * Adds a new string to the ring buffer.
     */
    public void addToRingBuffer() {
        _ringBufferPos = -1;
        _ringBuffer.add(0, _consoleInput.toString());
    }

    /**
     * Parses the console string and executes the command.
     */
    public void processConsoleString() {
        boolean success = false;

        try {
            success = Terasology.getInstance().getGroovyManager().runGroovyShell(_consoleInput.toString());
        } catch (Exception e) {
            Terasology.getInstance().getLogger().log(Level.INFO, e.getMessage());
        }

        if (success) {
            Terasology.getInstance().resetOpenGLParameters();
            Terasology.getInstance().getLogger().log(Level.INFO, "Console command \"{0}\" accepted.", _consoleInput);

            addToRingBuffer();
            resetDebugConsole();
            setVisible(false);
        } else {
            Terasology.getInstance().getLogger().log(Level.WARNING, "Console command \"{0}\" is invalid.", _consoleInput);
        }
    }

    public String toString() {
        return _consoleInput.toString();
    }

    /**
     * Resets the debug console.
     */
    public void resetDebugConsole() {
        _consoleInput.setLength(0);
    }

    @Override
    public void update() {
        super.update();

        _consoleText.setText(String.format("%s_", this));
        _consoleText.setPosition(new Vector2f(4, Display.getDisplayMode().getHeight() - 16 - 4));
    }
}
