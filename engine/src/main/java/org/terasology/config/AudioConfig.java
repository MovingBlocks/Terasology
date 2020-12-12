/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.config;

import org.terasology.config.flexible.AutoConfig;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.NumberRangeConstraint;

import static org.terasology.config.flexible.SettingArgument.constraint;
import static org.terasology.config.flexible.SettingArgument.defaultValue;
import static org.terasology.config.flexible.SettingArgument.type;

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
            defaultValue(0.5f),
            constraint(new NumberRangeConstraint<>(0.0f, 1.0f, true, true))
        );

    // TODO: Convert into Setting -- no uses yet
    private boolean disableSound;
}
