/*
 * Copyright 2012
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

package org.terasology.componentSystem.controllers;

import org.lwjgl.input.Keyboard;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.input.KeyDownEvent;
import org.terasology.events.input.binds.ConsoleButton;
import org.terasology.events.input.binds.InventoryButton;
import org.terasology.events.input.binds.PauseButton;
import org.terasology.game.CoreRegistry;
import org.terasology.input.ButtonState;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.menus.*;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class MenuControlSystem implements EventHandlerSystem {

    public static final String PAUSE_MENU = "engine:pauseMenu";
    public static final String INVENTORY = "engine:inventory";
    public static final String CONSOLE = "engine:console";

    @Override
    public void initialise() {
        GUIManager.getInstance().addWindow(new UIDebugConsole(), CONSOLE);
        GUIManager.getInstance().addWindow(new UIHeadsUpDisplay(), "engine:hud");
        GUIManager.getInstance().addWindow(new UIInventoryScreen(), INVENTORY);
        GUIManager.getInstance().addWindow(new UIPauseMenu(), PAUSE_MENU);
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onToggleConsole(ConsoleButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN && GUIManager.getInstance().toggleWindow(CONSOLE)) {
            event.consume();
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onToggleInventory(InventoryButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN && GUIManager.getInstance().toggleWindow(INVENTORY)) {
            event.consume();
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onTogglePause(PauseButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN && GUIManager.getInstance().toggleWindow(PAUSE_MENU)) {
            event.consume();
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        switch (event.getKey()) {
            case Keyboard.KEY_F12:
                CoreRegistry.get(WorldRenderer.class).printScreen();
                break;
        }
    }

}
