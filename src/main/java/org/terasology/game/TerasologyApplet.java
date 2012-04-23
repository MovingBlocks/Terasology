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

import org.terasology.game.modes.StateMainMenu;
import org.terasology.logic.manager.Config;

import java.applet.Applet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@SuppressWarnings("serial")
public final class TerasologyApplet extends Applet {
    private TerasologyEngine engine;
    private Thread gameThread;

    @Override
    public void init() {
        super.init();
        startGame();
    }

    private void startGame() {
        gameThread = new Thread() {
            @Override
            public void run() {
                try {
                    engine = new TerasologyEngine();
                    engine.run(new StateMainMenu());
                    engine.dispose();
                    // TODO: Move
                    Config.getInstance().saveConfig("SAVED_WORLDS/last.cfg");
                } catch (Exception e) {
                    Logger.getLogger(TerasologyApplet.class.getName()).log(Level.SEVERE, e.toString(), e);
                }
            }
        };

        gameThread.start();
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
        if (engine != null)
            engine.shutdown();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).severe("Failed to cleanly shut down engine");
        }

        super.destroy();
    }
}