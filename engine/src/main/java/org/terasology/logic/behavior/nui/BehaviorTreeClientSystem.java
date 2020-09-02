/*
 * Copyright 2020 MovingBlocks
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

package org.terasology.logic.behavior.nui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

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
