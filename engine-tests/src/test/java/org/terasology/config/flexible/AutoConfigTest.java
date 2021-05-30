// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.terasology.engine.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.terasology.engine.config.flexible.SettingArgument.constraint;
import static org.terasology.engine.config.flexible.SettingArgument.defaultValue;
import static org.terasology.engine.config.flexible.SettingArgument.description;
import static org.terasology.engine.config.flexible.SettingArgument.name;
import static org.terasology.engine.config.flexible.SettingArgument.type;

public class AutoConfigTest {
    @Test
    public void testGetSettings() {
        TestAutoConfig config = new TestAutoConfig();

        assertEquals(Sets.newHashSet(config.stringSetting, config.integerListSetting), config.getSettings());
    }

    @Test
    public void testGetSettingFieldsIn() {
        Set<Field> settingFields = AutoConfig.getSettingFieldsIn(TestAutoConfig.class);

        assertEquals(
            Sets.newHashSet("stringSetting", "integerListSetting"),
            settingFields.stream().map(Field::getName).collect(Collectors.toSet())
        );

        assertEquals(
            Sets.newHashSet(new TypeInfo<Setting<String>>() { }, new TypeInfo<Setting<ImmutableList<Integer>>>() { }),
            settingFields.stream().map(Field::getGenericType).map(TypeInfo::of).collect(Collectors.toSet())
        );
    }

    @Test
    public void testSettingDsl() {
        Double defaultValue = 10.0;
        NumberRangeConstraint<Double> constraint = new NumberRangeConstraint<>(0.0, 100.0, false, false);
        String name = "name";
        String description = "description";

        Setting<Double> setting = AutoConfig.setting(
            type(Double.class),
            defaultValue(defaultValue),
            constraint(constraint),
            name(name),
            description(description)
        );

        assertEquals(defaultValue, setting.getDefaultValue());
        assertEquals(constraint, setting.getConstraint());
        assertEquals(name, setting.getHumanReadableName());
        assertEquals(description, setting.getDescription());

        // The value should also be set to the default initially
        assertEquals(defaultValue, setting.get());
    }
}
