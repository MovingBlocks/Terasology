// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame;

import org.terasology.assets.ResourceUrn;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.SettingsMenuScreen;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.nui.WidgetUtil;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

/**
 * In-game menu that appears when the player presses `ESC` (by default) to open the menu system.
 *
 * In single player mode this also pauses the game time.
 */
public class PauseMenu extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:pauseMenu");

    @In
    private Time time;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "close", widget -> triggerBackAnimation());
        WidgetUtil.trySubscribe(this, "extra", widget -> triggerForwardAnimation(ExtraMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "settings", widget -> triggerForwardAnimation(SettingsMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "mainMenu", widget -> CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu()));
        WidgetUtil.trySubscribe(this, "exit", widget -> CoreRegistry.get(GameEngine.class).shutdown());

    }

    @Override
    public void onScreenOpened() {
        super.onScreenOpened();
        getManager().removeOverlay("engine:onlinePlayersOverlay");
    }

    @Override
    public void onClosed() {
        if (networkSystem.getMode() == NetworkMode.NONE) {
            time.setPaused(false);
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
