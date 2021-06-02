// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.internal;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.registry.In;
import org.terasology.input.Input;
import org.terasology.input.Keyboard;
import org.terasology.input.Keyboard.KeyId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RegisterSystem
public class BindCommands extends BaseComponentSystem {
    public static Map<Integer, SimpleUri> AZERTY;
    public static Map<Integer, SimpleUri> DVORAK;
    public static Map<Integer, SimpleUri> NEO;

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
