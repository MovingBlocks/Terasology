// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.NumberRangeConstraint;

import static org.terasology.engine.config.flexible.SettingArgument.constraint;
import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.name;
import static org.terasology.engine.config.flexible.SettingArgument.type;

public class AudioConfig extends AutoConfig {

    // TODO: Convert into Setting -- no uses yet
    private boolean disableSound;

    public final Setting<Float> soundVolume =
            setting(
                    type(Float.class),
                    defaultValue(1.0f),
                    name("${engine:menu#sound-volume}"),
                    // From AudioSettingsScreen
                    constraint(new NumberRangeConstraint<>(0.0f, 1.0f, true, true))
            );
    public final Setting<Float> musicVolume =
            setting(
                    type(Float.class),
                    defaultValue(0.5f),
                    name("${engine:menu#music-volume}"),
                    constraint(new NumberRangeConstraint<>(0.0f, 1.0f, true, true))
            );

    @Override
    public String getName() {
        return "${engine:menu#audio-settings-title}";
    }
}
