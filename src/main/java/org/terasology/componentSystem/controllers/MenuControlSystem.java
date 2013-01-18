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

package org.terasology.componentSystem.controllers;

import org.lwjgl.input.Keyboard;
import org.terasology.logic.players.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.NoHealthEvent;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.binds.ConsoleButton;
import org.terasology.input.binds.InventoryButton;
import org.terasology.input.binds.PauseButton;
import org.terasology.game.CoreRegistry;
import org.terasology.input.ButtonState;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class MenuControlSystem implements EventHandlerSystem {

    public static final String PAUSE_MENU = "pause";
    public static final String INVENTORY = "inventory";
    public static final String CHAT = "chat";
	public static final String HUD = "hud";

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onToggleConsole(ConsoleButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            CoreRegistry.get(GUIManager.class).openWindow(CHAT);
            event.consume();
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onToggleInventory(InventoryButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            CoreRegistry.get(GUIManager.class).openWindow(INVENTORY);
            event.consume();
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onTogglePause(PauseButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            CoreRegistry.get(GUIManager.class).openWindow(PAUSE_MENU);
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
    
    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
        CoreRegistry.get(GUIManager.class).openWindow("death");
    }

}
