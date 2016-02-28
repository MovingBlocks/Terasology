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

import org.terasology.utilities.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.loadProcesses.RegisterInputSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.logic.console.ConsoleSystem;
import org.terasology.logic.console.commands.CoreCommands;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.CanvasRenderer;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;

/**
 * The class implements the main game menu.
 * <br><br>
 *
 * @version 0.3
 */
public class StateMainMenu implements GameState {
    private Context context;
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
        context = gameEngine.createChildContext();
        CoreRegistry.setContext(context);

        //let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);

        eventSystem = context.get(EventSystem.class);
        context.put(Console.class, new ConsoleImpl(context));

        nuiManager = new NUIManagerInternal(context.get(CanvasRenderer.class), context);
        context.put(NUIManager.class, nuiManager);

        eventSystem.registerEventHandler(nuiManager);

        componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);

        // TODO: Reduce coupling between Input system and CameraTargetSystem,
        // TODO: potentially eliminating the following lines. See Issue #1126
        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        context.put(CameraTargetSystem.class, cameraTargetSystem);

        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");
        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");

        inputSystem = context.get(InputSystem.class);

        // TODO: REMOVE this and handle refreshing of core game state at the engine level - see Issue #1127
        new RegisterInputSystem(context).step();

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = new LocalPlayer();
        context.put(LocalPlayer.class, localPlayer);
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();

        playBackgroundMusic();

        //guiManager.openWindow("main");
        context.get(NUIManager.class).pushScreen("engine:mainMenuScreen");
        if (!messageOnLoad.isEmpty()) {
            nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error", messageOnLoad);
        }
    }

    @Override
    public void dispose() {
        eventSystem.process();

        componentSystemManager.shutdown();
        stopBackgroundMusic();
        nuiManager.clear();

        entityManager.clear();
    }

    private void playBackgroundMusic() {
        context.get(AudioManager.class).playMusic(Assets.getMusic("engine:MenuTheme").get());
    }

    private void stopBackgroundMusic() {
        context.get(AudioManager.class).stopAllSounds();
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
    public String getLoggingPhase() {
        return LoggingContext.MENU;
    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
    }

    private void updateUserInterface(float delta) {
        nuiManager.update(delta);
    }
}
