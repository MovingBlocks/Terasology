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
package org.terasology.config.flexible.settings;

import org.terasology.config.flexible.validators.SettingValueValidator;
import org.terasology.engine.SimpleUri;

public class IntegerSetting extends SettingImpl<Integer> {
    public IntegerSetting(SimpleUri id, Integer defaultValue) {
        super(id, defaultValue);
    }

    public IntegerSetting(SimpleUri id, Integer defaultValue, SettingValueValidator<Integer> validator) {
        super(id, defaultValue, validator);
    }

    @Override
    public void setValueFromString(String valueString) {
        value = Integer.parseInt(valueString);
    }
}
