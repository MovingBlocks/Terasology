/*
* Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.GameEngine;
import org.terasology.game.TerasologyEngine;
import org.terasology.game.modes.StateMainMenu;
import org.terasology.logic.manager.PathManager;
import org.terasology.ui.MainWindow;

import javax.swing.*;

/**
 * TeraEd main class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@SuppressWarnings("serial")
public final class TeraEd extends JWindow {

    private static final MainWindow mainWindow = new MainWindow();
    private static TerasologyEngine engine = new TerasologyEngine();
    private static final Logger logger = LoggerFactory.getLogger(TeraEd.class);

    public static void main(String[] args) {
        try {
            PathManager.getInstance().determineRootPath(true);

            TerasologyEngine.setRunningInEditorMode(true);
            engine.setCustomViewPort(mainWindow.getViewPort());

            engine.init();
            engine.run(new StateMainMenu());
            engine.dispose();
        } catch (Throwable t) {
            logger.error("Uncaught Exception", t);
        }
        System.exit(0);
    }

    public static GameEngine getEngine() {
        return engine;
    }

    public static MainWindow getMainWindow() {
        return mainWindow;
    }

}
