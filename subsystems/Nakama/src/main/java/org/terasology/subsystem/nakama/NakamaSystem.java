// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

/**
 * Entity system that bridges Gestalt chat events to the NakamaSubSystem.
 * Registered via NakamaSubSystem.registerSystems().
 */
@RegisterSystem
public class NakamaSystem extends BaseComponentSystem {
    private NakamaSubSystem nakamaSubSystem;

    public void setNakamaSubSystem(NakamaSubSystem subsystem) {
        this.nakamaSubSystem = subsystem;
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onChatMessage(ChatMessageEvent event, EntityRef entity) {
        if (nakamaSubSystem != null && nakamaSubSystem.isConnected()) {
            EntityRef from = event.getFrom();
            String playerName = "Unknown";
            DisplayNameComponent displayName = from.getComponent(DisplayNameComponent.class);
            if (displayName != null) {
                playerName = displayName.name;
            }
            nakamaSubSystem.sendChatMessage(playerName, event.getMessage());
        }
    }
}
