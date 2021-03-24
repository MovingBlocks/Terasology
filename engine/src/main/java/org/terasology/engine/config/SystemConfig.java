// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.LocaleConstraint;
import org.terasology.engine.config.flexible.constraints.NumberRangeConstraint;

import java.util.Locale;
import java.util.Locale.Category;
import java.util.Optional;

import static org.terasology.engine.config.flexible.SettingArgument.constraint;
import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.name;
import static org.terasology.engine.config.flexible.SettingArgument.override;
import static org.terasology.engine.config.flexible.SettingArgument.type;

public class SystemConfig extends AutoConfig {
    public static final String SAVED_GAMES_ENABLED_PROPERTY = "org.terasology.savedGamesEnabled";
    public static final String PERMISSIVE_SECURITY_ENABLED_PROPERTY = "org.terasology.permissiveSecurityEnabled";

    public final Setting<Long> dayNightLengthInMs = setting(
            type(Long.class),
            defaultValue(1800000L),
            name("Day/Night length (ms) (not yet)"),
            constraint(new NumberRangeConstraint<>(0L, Long.MAX_VALUE, false, false))
    );

    public final Setting<Integer> maxThreads = setting(
            type(Integer.class),
            defaultValue(Runtime.getRuntime().availableProcessors() - 1),
            name("Max threads(not yet)"),
            constraint(new NumberRangeConstraint<>(0, Integer.MAX_VALUE, false, false))
    );

    public final Setting<Integer> maxSecondsBetweenSaves = setting(
            type(Integer.class),
            defaultValue(60),
            name("Seconds between saves"),
            constraint(new NumberRangeConstraint<>(0, 1200, false, false))
    );

    public final Setting<Integer> maxUnloadedChunksPercentageTillSave = setting(
            type(Integer.class),
            defaultValue(40),
            name("Max unloaded chunks percentage till save"),
            constraint(new NumberRangeConstraint<>(0, 100, false, false))
    );

    public final Setting<Boolean> debugEnabled = setting(
            type(Boolean.class),
            defaultValue(false),
            name("Debug mode")
    );

    public final Setting<Boolean> monitoringEnabled = setting(
            type(Boolean.class),
            defaultValue(false),
            name("Monitoring")
    );

    public final Setting<Boolean> writeSaveGamesEnabled = setting(
            type(Boolean.class),
            defaultValue(true),
            name("Game saves writes"),
            override(() -> Optional.ofNullable(
                    System.getProperty(SAVED_GAMES_ENABLED_PROPERTY))
                    .map(Boolean::parseBoolean))
    );

    public final Setting<Long> chunkGenerationFailTimeoutInMs = setting(
            type(Long.class),
            defaultValue(1800000L),
            name("Chunk generation fail timeout (ms)"),
            constraint(new NumberRangeConstraint<>(0L, 3600000L, false, false))
    );

    public final Setting<Locale> locale = setting(
            type(Locale.class),
            defaultValue(Locale.getDefault(Category.DISPLAY)),
            name("${engine:menu#settings-language}"),
            constraint(new LocaleConstraint(Locale.getAvailableLocales())) // TODO provide translate project's locales (Pirate lang don't works)
    );

    @Override
    public String getName() {
        return "${engine:menu#system-settings-title}";
    }
}
