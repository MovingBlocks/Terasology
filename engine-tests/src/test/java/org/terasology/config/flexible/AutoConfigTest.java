/*
 * Copyright 2019 MovingBlocks
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
            Sets.newHashSet(new TypeInfo<Setting<String>>() {}, new TypeInfo<Setting<ImmutableList<Integer>>>() {}),
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
