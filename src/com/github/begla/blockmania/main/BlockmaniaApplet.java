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

    private final Blockmania _blockmania;
    private final Canvas _canvas;

    public BlockmaniaApplet() {
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
            }
        };

        _canvas.setSize(getWidth(), getHeight());

        add(_canvas);

        _canvas.setFocusable(true);
        _canvas.requestFocus();
        _canvas.setIgnoreRepaint(true);
    }

    private void startGame() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Display.setParent(_canvas);
                    Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(1024, 576));
                    Display.setTitle("Blockmania - Applet");
                    Display.create();

                    _blockmania.initControls();
                    _blockmania.initGame();
                    _blockmania.startGame();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        };

        t.start();
    }

    @Override
    public void init() {
        startGame();
    }

    @Override
    public void start() {
        _blockmania.unpauseGame();
    }

    @Override
    public void stop() {
        _blockmania.pauseGame();

    }

    @Override
    public void destroy() {
        remove(_canvas);
        super.destroy();
    }
}
