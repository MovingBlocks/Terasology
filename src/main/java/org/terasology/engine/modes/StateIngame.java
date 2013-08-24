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
package org.terasology.engine.modes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.TeraOVR;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.EventSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.game.Game;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.MenuControlSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.management.BlockManager;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

/**
 * Play mode.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */
public class StateIngame implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateIngame.class);

    private ComponentSystemManager componentSystemManager;
    private EventSystem eventSystem;
    private GUIManager guiManager;
    private WorldRenderer worldRenderer;
    private EngineEntityManager entityManager;
    private CameraTargetSystem cameraTargetSystem;
    private InputSystem inputSystem;
    private NetworkSystem networkSystem;

    /* GAME LOOP */
    private boolean pauseGame;


    public StateIngame() {
    }

    public void init(GameEngine engine) {
        guiManager = CoreRegistry.get(GUIManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        eventSystem = CoreRegistry.get(EventSystem.class);
        componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        entityManager = (EngineEntityManager) CoreRegistry.get(EntityManager.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        inputSystem = CoreRegistry.get(InputSystem.class);
        networkSystem = CoreRegistry.get(NetworkSystem.class);

        guiManager.openWindow(MenuControlSystem.HUD);

        if (CoreRegistry.get(Config.class).getRendering().isOculusVrSupport()
                && OculusVrHelper.isNativeLibraryLoaded()) {

            logger.info("Trying to initialize Oculus SDK...");
            TeraOVR.initSDK();

            logger.info("Updating Oculus projection parameters from device...");
            OculusVrHelper.updateFromDevice();
        }
        // Show or hide the HUD according to the settings
        final boolean hudHidden = CoreRegistry.get(Config.class).getRendering().getDebug().isHudHidden();
        for (UIDisplayElement element : CoreRegistry.get(GUIManager.class).getWindowById("hud").getDisplayElements()) {
            element.setVisible(!hudHidden);
        }
    }

    @Override
    public void dispose() {
        if (CoreRegistry.get(Config.class).getRendering().isOculusVrSupport() && OculusVrHelper.isNativeLibraryLoaded()) {
            logger.info("Shutting down Oculus SDK...");
            TeraOVR.clear();
        }

        boolean save = networkSystem.getMode().isAuthority();
        networkSystem.shutdown();
        // TODO: Shutdown background threads
        eventSystem.process();
        componentSystemManager.shutdown();
        guiManager.closeAllWindows();
        CoreRegistry.get(BulletPhysics.class).dispose();
        if (worldRenderer != null) {
            worldRenderer.dispose();
            worldRenderer = null;
        }

        if (save) {
            CoreRegistry.get(Game.class).save();
        }

        entityManager.clear();
        CoreRegistry.get(Console.class).dispose();
        CoreRegistry.clear();
        BlockManager.getAir().setEntity(EntityRef.NULL);

    }

    @Override
    public void update(float delta) {
        eventSystem.process();

        for (UpdateSubscriberSystem updater : componentSystemManager.iterateUpdateSubscribers()) {
            PerformanceMonitor.startActivity(updater.getClass().getSimpleName());
            updater.update(delta);
            PerformanceMonitor.endActivity();
        }

        if (worldRenderer != null && shouldUpdateWorld()) {
            worldRenderer.update(delta);
        }

        updateUserInterface();
    }

    @Override
    public void handleInput(float delta) {
        cameraTargetSystem.update(delta);
        inputSystem.update(delta);
    }

    private boolean shouldUpdateWorld() {
        return !pauseGame;
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (worldRenderer != null) {
            if (!CoreRegistry.get(Config.class).getRendering().isOculusVrSupport()) {
                worldRenderer.render(DefaultRenderingProcess.StereoRenderState.MONO);
            } else {
                worldRenderer.render(DefaultRenderingProcess.StereoRenderState.OCULUS_LEFT_EYE);
                worldRenderer.render(DefaultRenderingProcess.StereoRenderState.OCULUS_RIGHT_EYE);
            }
        }

        /* UI */
        PerformanceMonitor.startActivity("Render and Update UI");
        renderUserInterface();
        PerformanceMonitor.endActivity();
    }

    @Override
    public boolean isHibernationAllowed() {
        return networkSystem.getMode() == NetworkMode.NONE;
    }

    public void renderUserInterface() {
        guiManager.render();
    }

    private void updateUserInterface() {
        guiManager.update();
    }

    public void pause() {
        pauseGame = true;
    }

    public void unpause() {
        pauseGame = false;
    }

    public void togglePauseGame() {
        if (pauseGame) {
            unpause();
        } else {
            pause();
        }
    }

    public boolean isGamePaused() {
        return pauseGame;
    }

}
