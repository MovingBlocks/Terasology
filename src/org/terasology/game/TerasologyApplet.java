/*
* Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.terasology.game;

import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.ConfigurationManager;

import java.applet.Applet;
import java.util.logging.Level;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@SuppressWarnings("serial")
public final class TerasologyApplet extends Applet {
    private Terasology _terasology;
    private Thread _gameThread;

    @Override
    public void init() {
        startGame();
        super.init();
    }

    private void startGame() {
        _gameThread = new Thread() {
            @Override
            public void run() {
                try {
                    ConfigurationManager configuration = ConfigurationManager.getInstance();

                    Display.setParent(null);
                    Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(1280, 720));
                    Display.setTitle((String) configuration.getServerSetting("World.Info.title"));
                    Display.create();

                    _terasology = Terasology.getInstance();
                    _terasology.initControls();

                    _terasology.initGame();
                    _terasology.startGame();
                } catch (Exception e) {
                    Terasology.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
                }
            }
        };

        _gameThread.start();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void destroy() {
        if (_terasology != null)
            _terasology.exit(false);

        super.destroy();
    }
}