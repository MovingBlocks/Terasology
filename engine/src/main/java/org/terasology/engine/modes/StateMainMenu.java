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
import org.terasology.engine.modes.loadProcesses.RegisterInputSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.internal.ConsoleImpl;
import org.terasology.logic.console.internal.ConsoleSystem;
import org.terasology.logic.console.internal.CoreCommands;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.layers.mainMenu.ErrorMessagePopup;

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

        // let's get the entity event system running
        entityManager = new EntitySystemBuilder().build(CoreRegistry.get(ModuleManager.class),
               CoreRegistry.get(NetworkSystem.class), CoreRegistry.get(ReflectFactory.class), CoreRegistry.get(CopyStrategyLibrary.class));

        eventSystem = CoreRegistry.get(EventSystem.class);
        CoreRegistry.put(Console.class, new ConsoleImpl());

        nuiManager = CoreRegistry.get(NUIManager.class);
        ((NUIManagerInternal) nuiManager).refreshWidgetsLibrary();
        eventSystem.registerEventHandler(nuiManager);

        // TODO: REVIEW - As per conversation in https://github.com/MovingBlocks/Terasology/pull/1094:
        // TODO:          The ComponentSysteManager (CSM) is registered and becomes first available
        // TODO:          on engine initialization. However, it is re-instantiated and
        // TODO:          re-registered here, every time the application enters this game state.
        // TODO:          This is perhaps done to ensure a fresh new instance of the CSM that
        // TODO:          doesn't mantain references to previous game instances.
        // TODO:          If this is the case the code should make this strategy explicit.
        // TODO:          Furthermore, it needs to be evaluated if it is indeed necessary for the CSM
        // TODO:          to be instantiated and registered on engine initialization or if it should
        // TODO:          be done only in the context of game states and their lifecycles.
        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);

        // TODO: As per conversation in https://github.com/MovingBlocks/Terasology/pull/1094:
        // TODO: REVIEW - The input system has a dependency on the CameraTargetSystem.
        // TODO:          This coupling should be reduced, i.e. to avoid using it where there are no cameras.
        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class, cameraTargetSystem);

        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");
        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");

        inputSystem = CoreRegistry.get(InputSystem.class);

        // TODO: As per conversation in https://github.com/MovingBlocks/Terasology/pull/1094:
        // TODO: The RegisterInputSystem class duplicate instantiation and registration of the
        // TODO: CameraTargetSystem done above and the LocalPlayer's done below. Immortius says:
        // TODO: REMOVE this and handle refreshing of core game state at the engine level
        new RegisterInputSystem().step();

        // It is somewhat curious that the CameraTargetSystem and the LocalPlayer classes
        // are involved in the Main Menu, when no game is taking place and there are no
        // 3D elements requiring a camera yet. Apart from some existing dependencies
        // that will perhaps be eliminated, it turns out that immortius does have features
        // in mind that might involve 3D elements in the Main Menu. These features will
        // eventually require the classes mentioned.
        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = CoreRegistry.put(LocalPlayer.class, new LocalPlayer());
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();

        playBackgroundMusic();

        //guiManager.openWindow("main");
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
        nuiManager.clear();

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
