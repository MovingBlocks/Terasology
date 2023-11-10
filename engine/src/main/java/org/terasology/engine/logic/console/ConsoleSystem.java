// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.NetFilterEvent;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.binds.general.ConsoleButton;
import org.terasology.engine.logic.console.commandSystem.ConsoleCommand;
import org.terasology.engine.logic.console.ui.NotificationOverlay;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.ButtonState;

@RegisterSystem
public class ConsoleSystem extends BaseComponentSystem {

    @In
    private Console console;

    @In
    private NUIManager nuiManager;

    private NotificationOverlay overlay;

    @Override
    public void initialise() {
        if (nuiManager != null) {
            overlay = nuiManager.addOverlay(NotificationOverlay.ASSET_URI, NotificationOverlay.class);
            console.subscribe((Message message) -> {
                // make sure the message isn't already shown in the chat overlay
                if (!nuiManager.isOpen("engine:console")
                        && (message.getType() != CoreMessageType.CHAT
                        && message.getType() != CoreMessageType.NOTIFICATION
                        || !nuiManager.isOpen("engine:chat"))) {
                    overlay.setVisible(true);
                }
            });
        }
    }

    @Priority(EventPriority.PRIORITY_CRITICAL)
    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleConsole(ConsoleButton event, EntityRef entity) {
        if (event.getState() == ButtonState.UP) {
            nuiManager.toggleScreen("engine:console");
            overlay.setVisible(false);
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            console.addMessage(event.getFormattedMessage());
        }
    }

    @NetFilterEvent(netFilter = RegisterMode.AUTHORITY)
    @ReceiveEvent(components = ClientComponent.class)
    public void onCommand(CommandEvent event, EntityRef entity) {
        ConsoleCommand cmd = console.getCommand(event.getCommandName());

        if (cmd.getCommandParameters().size() >= cmd.getRequiredParameterCount() && cmd.isRunOnServer()) {
        console.execute(cmd.getName(), event.getParameters(), entity);
        }
    }
}
