/*
 * Copyright 2017 MovingBlocks
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

package org.terasology.logic.players;

import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commands.ServerCommands;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.NetworkMode;
import org.terasology.persistence.StorageManager;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.utilities.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.binds.general.LoadButton;
import org.terasology.input.binds.general.OnlinePlayersButton;
import org.terasology.input.binds.general.PauseButton;
import org.terasology.input.binds.general.SaveButton;
import org.terasology.input.binds.general.ScreenshotButton;
import org.terasology.logic.characters.events.DeathEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.OnlinePlayersOverlay;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.Manifest;

@RegisterSystem(RegisterMode.CLIENT)
public class MenuControlSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(GameProvider.class);

    @In
    private NUIManager nuiManager;

    @In
    private StorageManager storageManager;


    @Override
    public void initialise() {
        nuiManager.getHUD().addHUDElement("dropItemRegion");  //Ensure the drop region is behind the toolbar
        nuiManager.getHUD().addHUDElement("toolbar");
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onTogglePause(PauseButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen("engine:pauseMenu");
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onScreenshotCapture(ScreenshotButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            CoreRegistry.get(ScreenGrabber.class).takeScreenshot();
            CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:camera").get());
            event.consume();
        }
    }

    @Command(shortDescription = "Triggers the creation of a save game", runOnServer = true,
            requiredPermission = PermissionManager.SERVER_MANAGEMENT_PERMISSION)
    @ReceiveEvent(components = {ClientComponent.class})
    public void onSave(SaveButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            GameManifest latestManifest = getLatestGameManifest();
            storageManager.setSavePath(PathManager.getInstance().getSavePath(latestManifest.getTitle()).resolve(latestManifest.getTitle() + " Quick Save"));
            storageManager.requestSaving();
            event.consume();
        }
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void onLoad(LoadButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            GameManifest quickSaveGameManifest = getQuickSaveGameManifest();
            //quickSaveGameManifest.setTitle(quickSaveGameManifest.getTitle()+ " Quick Save");
            if (quickSaveGameManifest != null) {
                //CoreRegistry.get(GameEngine.class).changeState(new StateLoading(latestManifest, (loadingAsServer) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE)); not sure how to get the "loadingAsServer" boolean here
                boolean saveRequested = false;
                boolean isQuickLoad = true;
                CoreRegistry.get(GameEngine.class).changeState(new StateLoading(quickSaveGameManifest, NetworkMode.NONE, isQuickLoad), saveRequested); //quick load shouldn't cause the game to save
            }
            event.consume();
        }
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void onDeath(DeathEvent event, EntityRef entity) {
        if (entity.getComponent(ClientComponent.class).local) {
            nuiManager.pushScreen("engine:deathScreen");
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onShowOnlinePlayers(OnlinePlayersButton event, EntityRef entity) {
        boolean show = event.isDown();
        String onlinePlayersOverlay = "engine:onlinePlayersOverlay";

        if (show) {
            nuiManager.addOverlay(onlinePlayersOverlay, OnlinePlayersOverlay.class);
        } else {
            nuiManager.removeOverlay(onlinePlayersOverlay);
        }
        event.consume();
    }

    private static GameManifest getLatestGameManifest() {
        GameInfo latestGame = null;
        List<GameInfo> savedGames = GameProvider.getSavedGames();
        for (GameInfo savedGame : savedGames) {
            if (latestGame == null || savedGame.getTimestamp().after(latestGame.getTimestamp())) {
                latestGame = savedGame;
            }
        }

        if (latestGame == null) {
            return null;
        }

        return latestGame.getManifest();
    }

    private static GameManifest getQuickSaveGameManifest() {
        GameManifest latestManifest=getLatestGameManifest();
        try {
            String title = latestManifest.getTitle();
            return GameManifest.load(PathManager.getInstance().getSavePath(title).resolve(title + " Quick Save" + System.getProperty("file.separator") + "manifest.json"));
        } catch (IOException e) {
            logger.error("Failed to find Quick Load Save.", e);
            return null;
        }
    }
}
