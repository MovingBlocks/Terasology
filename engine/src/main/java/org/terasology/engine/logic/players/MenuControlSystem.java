// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.logic.players;

import org.terasology.audio.AudioManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.binds.general.OnlinePlayersButton;
import org.terasology.input.binds.general.PauseButton;
import org.terasology.input.binds.general.ScreenshotButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.events.PlayerDeathEvent;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.rendering.nui.layers.ingame.OnlinePlayersOverlay;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.utilities.Assets;


/**
 * This system controls the client's in-game menus (Pause screen, Death screen, HUDs and overlays).
 */
@RegisterSystem(RegisterMode.CLIENT)
public class MenuControlSystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;

    @In
    private Time time;

    @In
    private NetworkSystem networkSystem;

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
        if (networkSystem.getMode() == NetworkMode.NONE) {
            if (nuiManager.isOpen("engine:pauseMenu")) {
                time.setPaused(true);
            } else {
                time.setPaused(false);
            }
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

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onPlayerDeath(PlayerDeathEvent event, EntityRef character) {
        EntityRef client = character.getComponent(CharacterComponent.class).controller;
        if (client.getComponent(ClientComponent.class).local) {
            nuiManager.removeOverlay("engine:onlinePlayersOverlay");
            nuiManager.pushScreen("engine:deathScreen");
            if (event.damageTypeName != null) {
                ((DeathScreen) nuiManager.getScreen("engine:deathScreen")).setDeathDetails(event.instigatorName, event.damageTypeName);
            }
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
}
