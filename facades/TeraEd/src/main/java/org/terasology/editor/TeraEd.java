// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.editor;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.editor.properties.SceneProperties;
import org.terasology.editor.subsystem.AwtInput;
import org.terasology.editor.subsystem.LwjglPortlet;
import org.terasology.editor.ui.MainWindow;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.TerasologyEngineBuilder;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.config.BindsSubsystem;
import org.terasology.engine.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.subsystem.lwjgl.LwjglTimer;
import org.terasology.monitoring.PerformanceMonitor;

import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * TeraEd main class.
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
            LwjglPortlet portlet = new LwjglPortlet();

            PathManager.getInstance().useDefaultHomePath();

            engine = new TerasologyEngineBuilder()
                    .add(new LwjglTimer())
                    .add(new LwjglAudio())
                    .add(new AwtInput())
                    .add(new BindsSubsystem())
                    .add(portlet).build();

            if (!GLFW.glfwInit()) {
                throw new RuntimeException("Failed to initialize GLFW");
            }
            sceneProperties = new SceneProperties(engine);

            mainWindow = new MainWindow(this, engine);
            portlet.createCanvas();
            AWTGLCanvas canvas = portlet.getCanvas();

            engine.subscribeToStateChange(mainWindow);
            engine.initializeRun(new StateMainMenu());

            mainWindow.getViewport().setTerasology(canvas);

            portlet.initInputs();

            Runnable renderLoop = new Runnable() {
                public void run() {
                    if (canvas.isValid()) {
                        canvas.render();
                    }
                    SwingUtilities.invokeLater(this);
                }
            };

            // Setup swing thread as game thread
            PerformanceMonitor.startActivity("Other");
            SwingUtilities.invokeAndWait(portlet::setupThreads);
            SwingUtilities.invokeLater(renderLoop);
            PerformanceMonitor.endActivity();
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
