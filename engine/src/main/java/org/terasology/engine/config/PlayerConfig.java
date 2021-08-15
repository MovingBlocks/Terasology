// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.ColorConstraint;
import org.terasology.engine.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.engine.config.flexible.constraints.StringConstraint;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.CieCamColors;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.nui.Color;

import java.util.List;

import static org.terasology.engine.config.flexible.SettingArgument.constraint;
import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.name;
import static org.terasology.engine.config.flexible.SettingArgument.type;

public class PlayerConfig extends AutoConfig {

    private static final float DEFAULT_PLAYER_HEIGHT = 1.8f;

    private static final float DEFAULT_PLAYER_EYE_HEIGHT = 0.85f;

    public final Setting<String> playerName = setting(
            type(String.class),
            defaultValue(defaultPlayerName()),
            name("${engine:menu#player-name}"),
            constraint(new StringConstraint(
                    StringConstraint.notEmptyOrNull(),
                    StringConstraint.maxLength(100))
            )
    );
    public final Setting<Color> color = setting(
            type(Color.class),
            defaultValue(defaultPlayerColor()),
            name("${engine:menu#player-color}"),
            constraint(new ColorConstraint())
    );
    public final Setting<Float> height = setting(
            type(Float.class),
            defaultValue(DEFAULT_PLAYER_HEIGHT),
            name("${engine:menu#player-height}"),
            constraint(new NumberRangeConstraint<>(1.5f, 2.0f, true, true))
    );
    public final Setting<Float> eyeHeight = setting(
            type(Float.class),
            defaultValue(DEFAULT_PLAYER_EYE_HEIGHT),
            name("${engine:menu#player-eye-height}"),
            constraint(new NumberRangeConstraint<>(0.5f, 1.5f, true, true))
    );

    /**
     * Generates the player's default name. The default name is the string "Player" followed by a random 5 digit code
     * ranging from 10000 to 99999.
     *
     * @return a String with the player's default name.
     */
    private static String defaultPlayerName() {
        return "Player" + new FastRandom().nextInt(10000, 99999);
    }

    /**
     * Randomly generates a default color for the player via a random int generator using FastRandom object.
     *
     * @return a Color object with the player's default color.
     */
    private Color defaultPlayerColor() {
        Random rng = new FastRandom();
        List<Color> colors = CieCamColors.L65C65;
        return colors.get(rng.nextInt(colors.size()));
    }

    @Override
    public String getName() {
        return "${engine:menu#player-settings-title}";
    }
}
