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

import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.behavior.BehaviorSystem;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.mainMenu.ErrorMessagePopup;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

/**
 * The class implements the main game menu.
 * <p/>
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @version 0.3
 */
public class StateMainMenu implements GameState {
    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private ComponentSystemManager componentSystemManager;
    private NUIManager nuiManager;
    private InputSystem inputSystem;

    private String messageOnLoad = "";

    public StateMainMenu() {
    }

    public StateMainMenu(String showMessageOnLoad) {
        messageOnLoad = showMessageOnLoad;
    }


    @Override
    public void init(GameEngine gameEngine) {

        //lets get the entity event system running
        entityManager = new EntitySystemBuilder().build(CoreRegistry.get(ModuleManager.class), CoreRegistry.get(NetworkSystem.class), CoreRegistry.get(ReflectFactory.class));
        eventSystem = CoreRegistry.get(EventSystem.class);

        nuiManager = CoreRegistry.get(NUIManager.class);
        ((NUIManagerInternal) nuiManager).refreshElementsLibrary();

        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);

        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class, cameraTargetSystem);
        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");

        eventSystem.registerEventHandler(CoreRegistry.get(NUIManager.class));
        inputSystem = CoreRegistry.get(InputSystem.class);
        componentSystemManager.register(inputSystem, "engine:InputSystem");

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());

        LocalPlayer localPlayer = CoreRegistry.put(LocalPlayer.class, new LocalPlayer());
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();
        BehaviorSystem behaviorSystem = new BehaviorSystem();
        CoreRegistry.put(BehaviorSystem.class, behaviorSystem);
        behaviorSystem.initialise();

        playBackgroundMusic();

//        guiManager.openWindow("main");
        CoreRegistry.get(NUIManager.class).pushScreen("engine:mainMenuScreen");
        if (!messageOnLoad.isEmpty()) {
            nuiManager.pushScreen("engine:errorMessagePopup", ErrorMessagePopup.class).setError("Error", messageOnLoad);
        }
    }

    @Override
    public void dispose() {
        eventSystem.process();

        componentSystemManager.shutdown();
        stopBackgroundMusic();
        nuiManager.closeScreens();

        entityManager.clear();
        CoreRegistry.clear();
    }

    private void playBackgroundMusic() {
        CoreRegistry.get(AudioManager.class).playMusic(Assets.getMusic("engine:MenuTheme"));
    }

    private void stopBackgroundMusic() {
        CoreRegistry.get(AudioManager.class).stopAllSounds();
    }

    @Override
    public void handleInput(float delta) {
        inputSystem.update(delta);
    }

    @Override
    public void update(float delta) {
        updateUserInterface(delta);

        eventSystem.process();
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        nuiManager.render();
    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
    }

    private void updateUserInterface(float delta) {
        nuiManager.update(delta);
    }
}
