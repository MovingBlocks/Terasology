// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.behavior.nui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.CoreMessageType;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.ButtonState;

@RegisterSystem(RegisterMode.REMOTE_CLIENT)
public class BehaviorTreeClientSystem extends BaseComponentSystem {

    @In
    private Console console;

    /**
     * Called when a remote client presses F5 to open the Behavior Editor
     * @param event F5 press
     * @param entity the character entity reference
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleConsole(BTEditorButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            console.addMessage("Behavior Editor is only available to the Host or on Singleplayer", CoreMessageType.CHAT);
        }
    }

}
