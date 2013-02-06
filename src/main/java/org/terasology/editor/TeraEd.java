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
import org.terasology.editor.properties.SceneProperties;
import org.terasology.editor.ui.MainWindow;

import javax.swing.*;

/**
 * TeraEd main class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@SuppressWarnings("serial")
public final class TeraEd extends JWindow {

    private static MainWindow mainWindow;
    private static TerasologyEngine engine;
    private static final Logger logger = LoggerFactory.getLogger(TeraEd.class);

    private static final SceneProperties sceneProperties = new SceneProperties();

    public static void main(String[] args) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        engine = new TerasologyEngine();
        mainWindow = new MainWindow();

        try {
            PathManager.getInstance().determineRootPath(true);

            TerasologyEngine.setEditorAttached(true);
            engine.setCustomViewPort(mainWindow.getViewPort());

            engine.init();

            mainWindow.initPostEngine();

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

    public static SceneProperties getSceneProperties() {
        return sceneProperties;
    }
}
