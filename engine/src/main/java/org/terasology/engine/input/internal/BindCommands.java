// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.internal;

import com.google.common.collect.ImmutableMap;
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

@RegisterSystem
public class BindCommands extends BaseComponentSystem {
    public static final ImmutableMap<Integer, SimpleUri> AZERTY = new ImmutableMap.Builder<Integer, SimpleUri>()
            .put(KeyId.Z, new SimpleUri("engine:forwards"))
            .put(KeyId.S, new SimpleUri("engine:backwards"))
            .put(KeyId.Q, new SimpleUri("engine:left"))
            .build();
    public static final ImmutableMap<Integer, SimpleUri> DVORAK = new ImmutableMap.Builder<Integer, SimpleUri>()
            .put(KeyId.COMMA, new SimpleUri("engine:forwards"))
            .put(KeyId.A, new SimpleUri("engine:left"))
            .put(KeyId.O, new SimpleUri("engine:backwards"))
            .put(KeyId.E, new SimpleUri("engine:right"))
            .put(KeyId.C, new SimpleUri("engine:inventory"))
            .put(KeyId.PERIOD, new SimpleUri("engine:useItem"))
            .build();
    public static final ImmutableMap<Integer, SimpleUri> NEO = new ImmutableMap.Builder<Integer, SimpleUri>()
            .put(Keyboard.KeyId.V, new SimpleUri("engine:forwards"))
            .put(Keyboard.KeyId.I, new SimpleUri("engine:backwards"))
            .put(Keyboard.KeyId.U, new SimpleUri("engine:left"))
            .put(Keyboard.KeyId.A, new SimpleUri("engine:right"))
            .put(Keyboard.KeyId.L, new SimpleUri("engine:useItem"))
            .put(Keyboard.KeyId.G, new SimpleUri("engine:inventory"))
            .build();

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
