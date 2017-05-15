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
package org.terasology.input.internal;

import org.terasology.engine.SimpleUri;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.Input;
import org.terasology.input.Keyboard;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.registry.In;

/**
 */
@RegisterSystem
public class BindCommands extends BaseComponentSystem {

    @In
    private BindsManager bindsManager;

    @Command(shortDescription = "Maps a key to a function", requiredPermission = PermissionManager.NO_PERMISSION)
    public String bindKey(@CommandParam("key") String key, @CommandParam("function") String bind) {
        Input keyInput = Keyboard.Key.find(key);
        if (keyInput != null) {
            bindsManager.linkBindButtonToKey(keyInput.getId(), new SimpleUri(bind));
            StringBuilder builder = new StringBuilder();
            builder.append("Mapped ").append(keyInput.getDisplayName()).append(" to action ");
            builder.append(bind);
            return builder.toString();
        }
        throw new IllegalArgumentException("Unknown key: " + key);
    }

    @Command(shortDescription = "Switches to typical key binds for AZERTY",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String azerty() {
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.Z, new SimpleUri("engine:forwards"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.S, new SimpleUri("engine:backwards"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.Q, new SimpleUri("engine:left"));

        return "Changed key bindings to AZERTY keyboard layout.";
    }

    @Command(shortDescription = "Switches to typical keybinds for DVORAK",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String dvorak() {
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.COMMA, new SimpleUri("engine:forwards"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.A, new SimpleUri("engine:left"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.O, new SimpleUri("engine:backwards"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.E, new SimpleUri("engine:right"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.C, new SimpleUri("engine:inventory"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.PERIOD, new SimpleUri("engine:useItem"));

        return "Changed key bindings to DVORAK keyboard layout.";
    }

    @Command(shortDescription = "Switches to typical key binds for NEO 2 keyboard layout",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String neo() {
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.V, new SimpleUri("engine:forwards"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.I, new SimpleUri("engine:backwards"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.U, new SimpleUri("engine:left"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.A, new SimpleUri("engine:right"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.L, new SimpleUri("engine:useItem"));
        bindsManager.linkBindButtonToKey(Keyboard.KeyId.G, new SimpleUri("engine:inventory"));

        return "Changed key bindings to NEO 2 keyboard layout.";
    }
}
