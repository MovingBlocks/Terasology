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
import org.terasology.input.Keyboard.KeyId;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.registry.In;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
            return "Mapped " + keyInput.getDisplayName() + " to action " + bind;
        }
        throw new IllegalArgumentException("Unknown key: " + key);
    }

    public static Map<Integer, SimpleUri> AZERTY;
    public static Map<Integer, SimpleUri> DVORAK;
    public static Map<Integer, SimpleUri> NEO;

    static {
        AZERTY = new HashMap<>();
        AZERTY.put(KeyId.Z, new SimpleUri("engine:forwards"));
        AZERTY.put(KeyId.S, new SimpleUri("engine:backwards"));
        AZERTY.put(KeyId.Q, new SimpleUri("engine:left"));
        AZERTY = Collections.unmodifiableMap(AZERTY);

        DVORAK = new HashMap<>();
        DVORAK.put(KeyId.COMMA, new SimpleUri("engine:forwards"));
        DVORAK.put(KeyId.A, new SimpleUri("engine:left"));
        DVORAK.put(KeyId.O, new SimpleUri("engine:backwards"));
        DVORAK.put(KeyId.E, new SimpleUri("engine:right"));
        DVORAK.put(KeyId.C, new SimpleUri("engine:inventory"));
        DVORAK.put(KeyId.PERIOD, new SimpleUri("engine:useItem"));
        DVORAK = Collections.unmodifiableMap(DVORAK);

        NEO = new HashMap<>();
        NEO.put(Keyboard.KeyId.V, new SimpleUri("engine:forwards"));
        NEO.put(Keyboard.KeyId.I, new SimpleUri("engine:backwards"));
        NEO.put(Keyboard.KeyId.U, new SimpleUri("engine:left"));
        NEO.put(Keyboard.KeyId.A, new SimpleUri("engine:right"));
        NEO.put(Keyboard.KeyId.L, new SimpleUri("engine:useItem"));
        NEO.put(Keyboard.KeyId.G, new SimpleUri("engine:inventory"));
        NEO = Collections.unmodifiableMap(NEO);
    }

    @Command(shortDescription = "Switches to typical key binds for AZERTY",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String azerty() {
        AZERTY.forEach((key, bindId) -> bindsManager.linkBindButtonToKey(key, bindId));
        return "Changed key bindings to AZERTY keyboard layout.";
    }

    @Command(shortDescription = "Switches to typical key binds for DVORAK",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String dvorak() {
        DVORAK.forEach((key, bindId) -> bindsManager.linkBindButtonToKey(key, bindId));
        return "Changed key bindings to DVORAK keyboard layout.";
    }

    @Command(shortDescription = "Switches to typical key binds for NEO 2 keyboard layout",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String neo() {
        NEO.forEach((key, bindId) -> bindsManager.linkBindButtonToKey(key, bindId));
        return "Changed key bindings to NEO 2 keyboard layout.";
    }
}
