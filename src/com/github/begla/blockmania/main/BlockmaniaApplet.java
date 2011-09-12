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

/**
 * The heart and soul of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class BlockmaniaApplet extends Applet {

    private final Game _game;

    public BlockmaniaApplet() {
        _game = Game.getInstance();
        _game.setSandboxed(true);
    }

    private void startGame() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Display.setParent(null);
                    Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(1024, 576));
                    Display.setTitle("Blockmania - Applet");
                    Display.create();

                    _game.initControls();
                    _game.initGame();
                    _game.startGame();
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
        _game.unpauseGame();
    }

    @Override
    public void stop() {
        _game.pauseGame();

    }

    @Override
    public void destroy() {
        _game.stopGame();
    }
}
