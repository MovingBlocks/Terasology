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
package com.github.begla.blockmania.main;


import org.lwjgl.opengl.Display;

import java.applet.Applet;
import java.awt.*;

/**
 * The heart and soul of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class BlockmaniaApplet extends Applet {

    private Blockmania _blockmania;
    private Canvas _canvas;
    private Thread _gameThread;

    @Override
    public void init() {
        _blockmania = Blockmania.getInstance();
        _blockmania.setSandboxed(true);

        setLayout(new BorderLayout());

        _canvas = new Canvas() {
            @Override
            public void addNotify() {
                super.addNotify();
                startGame();
            }

            @Override
            public void removeNotify() {
                super.removeNotify();
                _blockmania.stopGame();

                try {
                    _gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        };

        _canvas.setSize(getWidth(), getHeight());

        add(_canvas);

        _canvas.setFocusable(true);
        _canvas.requestFocus();
        _canvas.setIgnoreRepaint(true);
    }

    private void startGame() {
        _gameThread = new Thread() {
            @Override
            public void run() {
                try {
                    Display.setParent(_canvas);
                    Display.create();

                    _blockmania.initControls();
                    _blockmania.initGame();
                    _blockmania.startGame();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        };

        _gameThread.start();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void destroy() {
        remove(_canvas);
        super.destroy();
    }
}
