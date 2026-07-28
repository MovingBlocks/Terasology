// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.config.flexible.Setting;

import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.name;
import static org.terasology.engine.config.flexible.SettingArgument.type;

/**
 * Persistent configuration for the Nakama subsystem.
 * Auto-discovered by the engine and saved to ~/.terasology/configs/nakama/NakamaAutoConfig.cfg
 */
public class NakamaAutoConfig extends AutoConfig {

    public final Setting<Boolean> enabled = setting(
            type(Boolean.class),
            defaultValue(false),
            name("Enable Nakama")
    );

    public final Setting<String> host = setting(
            type(String.class),
            defaultValue("localhost"),
            name("Nakama Server Host")
    );

    public final Setting<Integer> grpcPort = setting(
            type(Integer.class),
            defaultValue(7349),
            name("gRPC Port")
    );

    public final Setting<Integer> wsPort = setting(
            type(Integer.class),
            defaultValue(7350),
            name("WebSocket Port")
    );

    public final Setting<String> channel = setting(
            type(String.class),
            defaultValue("bifrost.lobby"),
            name("Chat Channel")
    );

    public final Setting<String> playerName = setting(
            type(String.class),
            defaultValue(""),
            name("Player Name")
    );

    @Override
    public String getName() {
        return "Nakama Settings";
    }
}
