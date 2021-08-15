// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.nameTags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.ClientInfoComponent;
import org.terasology.engine.network.ColorComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.nui.Color;
import org.terasology.engine.registry.In;


/**
 * Creates name tags for characters controlled by players based on their name.
 *
 * Once there is the intention to implement multiple different name tag generation rules for players feel free to move
 * this system into a module so that it isn't always enabled.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class PlayerNameTagSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(PlayerNameTagSystem.class);

    @In
    private NetworkSystem networkSystem;

    /**
     * Listening for {@link OnPlayerSpawnedEvent} does not work, as it is an
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
            return; // the character is not owned by a client (no other player)
        }
        if (clientComponent.local) {
            return; // the character belongs to the local player and does not need a name tag
        }

        EntityRef clientInfoEntity = clientComponent.clientInfo;

        DisplayNameComponent displayNameComponent = clientInfoEntity.getComponent(DisplayNameComponent.class);
        if (displayNameComponent == null) {
            logger.debug("Cannot create name tag for client without DisplayNameComponent");
            return;
        }
        String name = displayNameComponent.name;

        float yOffset = characterComponent.nameTagOffset;

        ColorComponent colorComponent = clientInfoEntity.getComponent(ColorComponent.class);
        final Color color = colorComponent != null ? colorComponent.color : Color.WHITE;

        characterEntity.upsertComponent(NameTagComponent.class, maybeNameTag -> {
            NameTagComponent nameTagComponent = maybeNameTag.orElse(new NameTagComponent());
            nameTagComponent.text = name;
            nameTagComponent.textColor = color;
            nameTagComponent.yOffset = yOffset;
            return nameTagComponent;
        });
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
                                    DisplayNameComponent displayNameComponent, ClientInfoComponent clientInfoComp) {

        EntityRef clientEntity = clientInfoComp.client;
        if (!clientEntity.exists()) {
            return; // offline players aren't visible: nothing to do
        }

        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        if (clientComponent == null) {
            return; // the character is not owned by a client (no other player)
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
