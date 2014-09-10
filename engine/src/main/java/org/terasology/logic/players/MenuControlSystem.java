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

package org.terasology.logic.players;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.Keyboard;
import org.terasology.input.binds.general.PauseButton;
import org.terasology.input.binds.inventory.InventoryButton;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.logic.characters.events.DeathEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.layers.ingame.inventory.TransferItemCursor;
import org.terasology.rendering.opengl.DefaultRenderingProcess;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.CLIENT)
public class MenuControlSystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;

    @Override
    public void initialise() {
        nuiManager.getHUD().addHUDElement("toolbar");
        TransferItemCursor cursor = new TransferItemCursor();
        UIElement cursorElement = Assets.generateAsset(new AssetUri(AssetType.UI_ELEMENT, "engine:transferItemCursor"), new UIData(cursor), UIElement.class);
        nuiManager.addOverlay(cursorElement, ControlWidget.class);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleInventory(InventoryButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen("engine:inventoryScreen");
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onTogglePause(PauseButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen("engine:pauseMenu");
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        switch (event.getKey().getId()) {
            case Keyboard.KeyId.F12:
                DefaultRenderingProcess.getInstance().takeScreenshot();
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:camera"));
                break;
            default:
                break;
        }
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void onDeath(DeathEvent event, EntityRef entity) {
        if (entity.getComponent(ClientComponent.class).local) {
            nuiManager.pushScreen("engine:deathScreen");
        }
    }

}
