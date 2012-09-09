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

import java.util.Iterator;

import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.windows.*;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

/**
 * The class implements the main game menu.
 * <p/>
 * TODO: Add screen "Multiplayer"
 * TODO: Add animated background
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @version 0.3
 */
public class StateMainMenu implements GameState {
    private GameEngine _gameInstance = null;
    
    private PersistableEntityManager entityManager;
    private EventSystem eventSystem;
    private InputSystem inputSystem;
    private ComponentSystemManager componentSystemManager;
    private CameraTargetSystem cameraTargetSystem;

    @Override
    public void init(GameEngine gameEngine) {
        _gameInstance = gameEngine;
        
        //lets get the entity event system running
        entityManager = new EntitySystemBuilder().build();
        eventSystem = CoreRegistry.get(EventSystem.class);

        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
        
        cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class, cameraTargetSystem);
        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");
        
        inputSystem = new InputSystem();
        CoreRegistry.put(InputSystem.class, inputSystem);
        componentSystemManager.register(inputSystem, "engine:InputSystem");
        
        LocalPlayerComponent localPlayerComp = new LocalPlayerComponent();
        CoreRegistry.put(LocalPlayerComponent.class, localPlayerComp);
        entityManager.create(localPlayerComp);
        
        CoreRegistry.put(LocalPlayer.class, new LocalPlayer(EntityRef.NULL));
    }
    
    @Override
    public void activate() {
        for (ComponentSystem system : componentSystemManager.iterateAll()) {
            system.initialise();
        }
        
        Iterator<EntityRef> iterator = entityManager.iteratorEntities(LocalPlayerComponent.class).iterator();
        if (iterator.hasNext()) {
            CoreRegistry.get(LocalPlayer.class).setEntity(iterator.next());
        }
        
        playBackgroundMusic();
        
        GUIManager.getInstance().openWindow("main");
    }

    @Override
    public void deactivate() {
        eventSystem.process();
        
        for (ComponentSystem system : componentSystemManager.iterateAll()) {
            system.shutdown();
        }
        
        stopBackgroundMusic();
        GUIManager.getInstance().closeAllWindows();
        
        entityManager.clear();
    }
    
    @Override
    public void dispose() {
        // Nothing to do here.
    }

    private void playBackgroundMusic() {
        AudioManager.playMusic("engine:resurface");
    }

    private void stopBackgroundMusic() {
        AudioManager.getInstance().stopAllSounds();
    }

    @Override
    public void handleInput(float delta) {
        inputSystem.update(delta);
    }

    @Override
    public void update(float delta) {
        updateUserInterface();
        
        eventSystem.process();
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        renderUserInterface();
    }

    public void renderUserInterface() {
        GUIManager.getInstance().render();
    }

    private void updateUserInterface() {
        GUIManager.getInstance().update();
    }
}
