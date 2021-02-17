// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.config;

import org.terasology.config.flexible.AutoConfig;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.NumberRangeConstraint;

import java.util.Locale;
import java.util.Locale.Category;
import java.util.Optional;

import static org.terasology.config.flexible.SettingArgument.constraint;
import static org.terasology.config.flexible.SettingArgument.defaultValue;
import static org.terasology.config.flexible.SettingArgument.override;
import static org.terasology.config.flexible.SettingArgument.type;

public class SystemConfig extends AutoConfig {
    public static final String SAVED_GAMES_ENABLED_PROPERTY = "org.terasology.savedGamesEnabled";
    public static final String PERMISSIVE_SECURITY_ENABLED_PROPERTY = "org.terasology.permissiveSecurityEnabled";

    public final Setting<Long> dayNightLengthInMs = setting(
            type(Long.class),
            defaultValue(1800000L),
            constraint(new NumberRangeConstraint<>(0L, Long.MAX_VALUE, false, false))
    );

    public final Setting<Integer> maxThreads = setting(
            type(Integer.class),
            defaultValue(Runtime.getRuntime().availableProcessors() - 1),
            constraint(new NumberRangeConstraint<>(0, Integer.MAX_VALUE, false, false))
    );

    public final Setting<Integer> maxSecondsBetweenSaves = setting(
            type(Integer.class),
            defaultValue(60),
            constraint(new NumberRangeConstraint<>(0, Integer.MAX_VALUE, false, false))
    );

    public final Setting<Integer> maxUnloadedChunksPercentageTillSave = setting(
            type(Integer.class),
            defaultValue(40),
            constraint(new NumberRangeConstraint<>(0, 100, false, false))
    );

    public final Setting<Boolean> debugEnabled = setting(
            type(Boolean.class),
            defaultValue(false)
    );

    public final Setting<Boolean> monitoringEnabled = setting(
            type(Boolean.class),
            defaultValue(false)
    );

    public final Setting<Boolean> writeSaveGamesEnabled = setting(
            type(Boolean.class),
            defaultValue(true),
            override(() -> Optional.ofNullable(
                    System.getProperty(SAVED_GAMES_ENABLED_PROPERTY))
                    .map(Boolean::parseBoolean))
    );

    public final Setting<Long> chunkGenerationFailTimeoutInMs = setting(
            type(Long.class),
            defaultValue(1800000L),
            constraint(new NumberRangeConstraint<>(0L, Long.MAX_VALUE, false, false))
    );

    public final Setting<Locale> locale = setting(
            type(Locale.class),
            defaultValue(Locale.getDefault(Category.DISPLAY))
    );
}
