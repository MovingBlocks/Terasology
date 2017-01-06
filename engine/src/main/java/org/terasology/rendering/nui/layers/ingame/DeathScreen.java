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

import org.slf4j.LoggerFactory;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.RespawnRequestEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.WidgetUtil;
import org.slf4j.Logger;

/**
 */
public class DeathScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(CharacterComponent.class);

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public void initialise() {
        CharacterComponent character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CharacterComponent.class);
        UILabel causeOfDeath = find("causeOfDeath", UILabel.class);
        if(character.damageType != "" && character.damageType != null) {
            causeOfDeath.setText("You died due to " + character.damageType + ".\n");
        }
        if(character.instigator != "" && character.instigator != null) {
            causeOfDeath.setText(causeOfDeath.getText() + character.instigator + " killed you\n");
        }
        if(character.directCause != "" && character.directCause != null) {
            causeOfDeath.setText(causeOfDeath.getText() + "You died because of " + character.directCause + "\n");
        }
        WidgetUtil.trySubscribe(this, "respawn", widget -> {
            CoreRegistry.get(LocalPlayer.class).getClientEntity().send(new RespawnRequestEvent());
            getManager().closeScreen(DeathScreen.this);
        });
        WidgetUtil.trySubscribe(this, "settings", widget -> getManager().pushScreen("settingsMenuScreen"));
        WidgetUtil.trySubscribe(this, "mainMenu", widget -> {
            CoreRegistry.get(LocalPlayer.class).getClientEntity().send(new RespawnRequestEvent());
            CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu());
        });
        WidgetUtil.trySubscribe(this, "exitGame", widget -> CoreRegistry.get(GameEngine.class).shutdown());
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
