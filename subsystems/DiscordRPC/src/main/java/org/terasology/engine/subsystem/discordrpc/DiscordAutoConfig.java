// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.subsystem.discordrpc;

import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.config.flexible.Setting;

import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.name;
import static org.terasology.engine.config.flexible.SettingArgument.type;

public class DiscordAutoConfig extends AutoConfig {

    public final Setting<Boolean> discordPresence =
            setting(
                    type(Boolean.class),
                    defaultValue(true),
                    name("${engine:menu#discord-presence}")
            );


    @Override
    public String getName() {
        return "${engine:menu#discord-settings-title}";
    }
}
