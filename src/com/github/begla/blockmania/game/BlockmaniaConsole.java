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
package com.github.begla.blockmania.game;

import javolution.util.FastList;
import org.lwjgl.input.Keyboard;

import java.util.logging.Level;

/**
 * The debug console of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class BlockmaniaConsole {

    private final StringBuffer _consoleInput = new StringBuffer();
    private Blockmania _parent;
    private FastList<String> _ringBuffer = new FastList<String>();
    private int _ringBufferPos = -1;

    /**
     * Init. a new Blockmania console.
     *
     * @param parent
     */
    public BlockmaniaConsole(Blockmania parent) {
        _parent = parent;
    }

    protected void processKeyboardInput(int key) {
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

    public void addToRingBuffer() {
        _ringBufferPos = -1;
        _ringBuffer.addFirst(_consoleInput.toString());
    }

    /**
     * Parses the console string and executes the command.
     */

    public void processConsoleString() {
        boolean success = false;

        try {
            success = _parent.getGroovyManager().runGroovyShell(_consoleInput.toString());
        } catch (Exception e) {
            Blockmania.getInstance().getLogger().log(Level.INFO, e.getMessage());
        }

        if (success) {
            Blockmania.getInstance().resetOpenGLParameters();
            Blockmania.getInstance().getLogger().log(Level.INFO, "Console command \"{0}\" accepted.", _consoleInput);

            addToRingBuffer();
            resetDebugConsole();
        } else {
            Blockmania.getInstance().getLogger().log(Level.WARNING, "Console command \"{0}\" is invalid.", _consoleInput);
        }
    }

    public String toString() {
        return _consoleInput.toString();
    }

    /**
     * Disables/enables the debug console.
     */
    public void resetDebugConsole() {
        _consoleInput.setLength(0);
    }

}
