/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.game.modes;

import com.leapmotion.leap.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.componentSystem.controllers.MenuControlSystem;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.TerasologyConstants;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.InputSystem;
import org.terasology.input.TeraLeapSystem;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import java.io.File;
import java.io.IOException;

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
public class StateSinglePlayer implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateSinglePlayer.class);

    private ComponentSystemManager componentSystemManager;
    private EventSystem eventSystem;
    private GUIManager guiManager;
    private WorldRenderer worldRenderer;
    private EntityManager entityManager;
    private CameraTargetSystem cameraTargetSystem;
    private InputSystem inputSystem;
    private TeraLeapSystem teraLeapSystem;

    /* GAME LOOP */
    private boolean pauseGame = false;

    public StateSinglePlayer() {

    }

    public void init(GameEngine engine) {
        guiManager = CoreRegistry.get(GUIManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        eventSystem = CoreRegistry.get(EventSystem.class);
        componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        entityManager = CoreRegistry.get(EntityManager.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        inputSystem = CoreRegistry.get(InputSystem.class);

        //TODO: Put this somewhere better?
        teraLeapSystem = new TeraLeapSystem();
        teraLeapSystem.initialise();

        guiManager.openWindow(MenuControlSystem.HUD);
    }

    @Override
    public void dispose() {
        // TODO: Shutdown background threads
        eventSystem.process();
        for (ComponentSystem system : componentSystemManager.iterateAll()) {
            system.shutdown();
        }
        componentSystemManager.clear();
        guiManager.closeAllWindows();
        try {
            CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getWorldSavePath(CoreRegistry.get(WorldProvider.class).getTitle()), TerasologyConstants.ENTITY_DATA_FILE), WorldPersister.SaveFormat.Binary);
        } catch (IOException e) {
            logger.error("Failed to save entities", e);
        }
        entityManager.clear();
        if (worldRenderer != null) {
            worldRenderer.dispose();
            worldRenderer = null;
        }
    }

    @Override
    public void update(float delta) {
        /* GUI */
        updateUserInterface();

        eventSystem.process();

        for (UpdateSubscriberSystem updater : componentSystemManager.iterateUpdateSubscribers()) {
            PerformanceMonitor.startActivity(updater.getClass().getSimpleName());
            updater.update(delta);
        }

        if (worldRenderer != null && shouldUpdateWorld()) {
            worldRenderer.update(delta);
        }
    }

    @Override
    public void handleInput(float delta) {
        cameraTargetSystem.update();
        inputSystem.update(delta);
        teraLeapSystem.update(delta); //TODO: Any way to get update() invoked without this? Module style
    }

    private boolean shouldUpdateWorld() {
        return !pauseGame;
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (worldRenderer != null) {
            worldRenderer.render();
        }

        /* UI */
        PerformanceMonitor.startActivity("Render and Update UI");
        renderUserInterface();
        PerformanceMonitor.endActivity();
    }

    public void renderUserInterface() {
        guiManager.render();
    }

    private void updateUserInterface() {
        guiManager.update();
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
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
