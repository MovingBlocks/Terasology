/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.console;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.binds.general.ConsoleButton;
import org.terasology.logic.console.commandSystem.ConsoleCommand;
import org.terasology.logic.console.ui.NotificationOverlay;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem
public class ConsoleSystem extends BaseComponentSystem {

    @In
    private Console console;

    @In
    private NUIManager nuiManager;

    private NotificationOverlay overlay;

    @Override
    public void initialise() {
        overlay = nuiManager.addOverlay(NotificationOverlay.ASSET_URI, NotificationOverlay.class);
        console.subscribe((Message message) -> {
            if (!nuiManager.isOpen("engine:console")) {
                // make sure the message isn't already shown in the chat overlay
                if (message.getType() != CoreMessageType.CHAT && message.getType() != CoreMessageType.NOTIFICATION
                        || !nuiManager.isOpen("engine:chat")) {
                    overlay.setVisible(true);
                }
            }
        });
    }

    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
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

    @ReceiveEvent(components = ClientComponent.class, netFilter = RegisterMode.AUTHORITY)
    public void onCommand(CommandEvent event, EntityRef entity) {
        ConsoleCommand cmd = console.getCommand(event.getCommandName());

        if (cmd.getCommandParameters().size() >= cmd.getRequiredParameterCount() && cmd.isRunOnServer()) {
        console.execute(cmd.getName(), event.getParameters(), entity);
        }
    }
}
