/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.nameTags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.network.ClientComponent;
import org.terasology.network.ClientInfoComponent;
import org.terasology.network.ColorComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Color;


/**
 * Creates name tags for characters controlled by players based on their name.
 *
 * Once there is the intention to implement multiple different name tag generation rules for players feel free to move
 * this system into a module so that it isn't always enabled.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class PlayerNameTagSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(NameTagClientSystem.class);

    @In
    private NetworkSystem networkSystem;

    /**
     * Listening for {@link org.terasology.logic.players.event.OnPlayerSpawnedEvent} does not work, as it is an
     * authority event that does not get processed at clients. That is why we listen for the activation.
     */
    @ReceiveEvent(components = CharacterComponent.class)
    public void onCharacterActivation(OnActivatedComponent event, EntityRef characterEntity,
                                      CharacterComponent characterComponent) {
        EntityRef ownerEntity = networkSystem.getOwnerEntity(characterEntity);
        if (ownerEntity == null) {
            return; // NPC
        }

        ClientComponent clientComponent = ownerEntity.getComponent(ClientComponent.class);
        if (clientComponent == null) {
            logger.warn("Can't create player based name tag for character as owner has no client component");
            return;
        }
        if (clientComponent.local) {
            return; // the character belongs to the local player and does not need a name tag
        }

        EntityRef clientInfoEntity = clientComponent.clientInfo;

        DisplayNameComponent displayNameComponent = clientInfoEntity.getComponent(DisplayNameComponent.class);
        if (displayNameComponent == null) {
            logger.error("Can't create player based name tag for character as client info has no DisplayNameComponent");
            return;
        }
        String name = displayNameComponent.name;

        float yOffset = characterComponent.nameTagOffset;

        Color color = Color.WHITE;
        ColorComponent colorComponent = clientInfoEntity.getComponent(ColorComponent.class);
        if (colorComponent != null) {
            color = colorComponent.color;
        }

        NameTagComponent nameTagComponent = characterEntity.getComponent(NameTagComponent.class);
        boolean newComponent = nameTagComponent == null;
        if (nameTagComponent == null) {
            nameTagComponent = new NameTagComponent();
        }
        nameTagComponent.text = name;
        nameTagComponent.textColor = color;
        nameTagComponent.yOffset = yOffset;
        if (newComponent) {
            characterEntity.addComponent(nameTagComponent);
        } else {
            characterEntity.saveComponent(nameTagComponent);
        }

    }

    /**
     * The player entity may currently become "local" afterh the player has been activated.
     *
     * To address this issue the name tag component will be removed again when a client turns out to be local
     * afterwards.
     */
    @ReceiveEvent
    public void onClientComponentChange(OnChangedComponent event, EntityRef clientEntity,
                                    ClientComponent clientComponent) {
        if (clientComponent.local) {
            EntityRef character = clientComponent.character;
            if (character.exists() && character.hasComponent(NameTagComponent.class)) {
                character.removeComponent(NameTagComponent.class);
            }
        }
    }

    @ReceiveEvent
    public void onDisplayNameChange(OnChangedComponent event, EntityRef clientInfoEntity,
                                    DisplayNameComponent displayNameComponent) {
        ClientInfoComponent clientInfoComp = clientInfoEntity.getComponent(ClientInfoComponent.class);
        if (clientInfoComp == null) {
            return; // not a client info object
        }

        EntityRef clientEntity = clientInfoComp.client;
        if (!clientEntity.exists()) {
            return; // offline players aren't visible: nothing to do
        }

        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        if (clientComponent == null) {
            logger.warn("Can't update name tag as client entity lacks ClietnComponent");
            return;
        }

        EntityRef characterEntity = clientComponent.character;
        if (characterEntity == null || !characterEntity.exists()) {
            return; // player has no character, nothing to do
        }

        NameTagComponent nameTagComponent = characterEntity.getComponent(NameTagComponent.class);
        if (nameTagComponent == null) {
            return; // local players don't have a name tag
        }

        nameTagComponent.text = displayNameComponent.name;
        characterEntity.saveComponent(nameTagComponent);
    }
}
