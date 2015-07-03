/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.editor;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.editor.properties.SceneProperties;
import org.terasology.editor.ui.MainWindow;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.subsystem.lwjgl.LwjglCustomViewPort;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.subsystem.lwjgl.LwjglTimer;

import javax.swing.*;
import java.util.Collection;

/**
 * TeraEd main class.
 *
 * @author Benjamin Glatzel
 */
@SuppressWarnings("serial")
public final class TeraEd extends JWindow {

    private MainWindow mainWindow;
    private TerasologyEngine engine;
    private final Logger logger = LoggerFactory.getLogger(TeraEd.class);

    private SceneProperties sceneProperties;

    public static void main(String[] args) {
        new TeraEd().run();
    }

    public void run() {
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
            logger.warn("Failed to set look and feel to Nimbus", e);
        }
        try {
            LwjglCustomViewPort lwjglCustomViewPort = new LwjglCustomViewPort();
            Collection<EngineSubsystem> subsystemList = Lists.<EngineSubsystem>newArrayList(new LwjglGraphics(), new LwjglTimer(), new LwjglAudio(), new LwjglInput(),
                    lwjglCustomViewPort);

            PathManager.getInstance().useDefaultHomePath();

            engine = new TerasologyEngine(subsystemList);
            sceneProperties = new SceneProperties(engine);
            mainWindow = new MainWindow(this, engine);
            lwjglCustomViewPort.setCustomViewport(mainWindow.getViewport());

            engine.setHibernationAllowed(false);
            engine.subscribeToStateChange(mainWindow);

            engine.run(new StateMainMenu());
        } catch (Throwable t) {
            logger.error("Uncaught Exception", t);
        }
    }

    public GameEngine getEngine() {
        return engine;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public SceneProperties getSceneProperties() {
        return sceneProperties;
    }
}
