/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.engine.TerasologyEngine;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.players.MenuControlSystem;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.event.EventSystem;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.manager.GUIManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.physics.BulletPhysics;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.ChunkProvider;

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
    private boolean pauseGame = false;


    public StateIngame() {
    }

    public void init(GameEngine engine) {
        guiManager = CoreRegistry.get(GUIManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        eventSystem = CoreRegistry.get(EventSystem.class);
        componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        entityManager = (EngineEntityManager)CoreRegistry.get(EntityManager.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        inputSystem = CoreRegistry.get(InputSystem.class);
        networkSystem = CoreRegistry.get(NetworkSystem.class);

        guiManager.openWindow(MenuControlSystem.HUD);
    }

    @Override
    public void dispose() {
        boolean saveWorld = CoreRegistry.get(NetworkSystem.class).getMode().isAuthority();
        networkSystem.shutdown();
        // TODO: Shutdown background threads
        eventSystem.process();
        componentSystemManager.shutdown();
        guiManager.closeAllWindows();
        CoreRegistry.get(BulletPhysics.class).dispose();
        if (saveWorld) {
            try {
                CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getCurrentWorldPath(), TerasologyConstants.ENTITY_DATA_FILE), WorldPersister.SaveFormat.Binary);
            } catch (IOException e) {
                logger.error("Failed to save entities", e);
            }
        }
        entityManager.clear();
        if (worldRenderer != null) {
            worldRenderer.dispose(saveWorld);
            worldRenderer = null;
        }
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
        cameraTargetSystem.update();
        inputSystem.update(delta);
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
