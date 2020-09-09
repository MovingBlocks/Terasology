// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.NumberRangeConstraint;

import static org.terasology.engine.config.flexible.SettingArgument.constraint;
import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.type;

public class AudioConfig extends AutoConfig {
    public final Setting<Float> soundVolume =
        setting(
            type(Float.class),
            defaultValue(1.0f),
            // From AudioSettingsScreen
            constraint(new NumberRangeConstraint<>(0.0f, 1.0f, true, true))
        );

    public final Setting<Float> musicVolume =
        setting(
            type(Float.class),
            defaultValue(1.0f),
            constraint(new NumberRangeConstraint<>(0.0f, 1.0f, true, true))
        );

    // TODO: Convert into Setting -- no uses yet
    private boolean disableSound;
}
