/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame;

import org.terasology.assets.ResourceUrn;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.settings.SettingsMenuScreen;

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
