// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible;

import com.google.common.collect.ImmutableList;
import org.terasology.reflection.TypeInfo;

import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.name;
import static org.terasology.engine.config.flexible.SettingArgument.type;

public class TestAutoConfig extends AutoConfig {
    public final Setting<String> stringSetting = setting(
        type(String.class),
        defaultValue(""),
        name("Human Readable Name")
    );

    public final Setting<ImmutableList<Integer>> integerListSetting = setting(
        type(new TypeInfo<ImmutableList<Integer>>() { }),
        defaultValue(ImmutableList.of())
    );

    @Override
    public String getName() {
        return "Test Auto Config";
    }
}
