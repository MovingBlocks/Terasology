/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.config.flexible;

import com.google.common.collect.Maps;
import org.terasology.engine.SimpleUri;

import java.util.Map;

public class FlexibleConfig {
    private Map<SimpleUri, Setting> settingMap;

    public FlexibleConfig() {
        this.settingMap = Maps.newHashMap();
    }

    public <V> boolean add(Setting<V> setting) {
        SimpleUri key = setting.getId();

        if (key == null || has(key))
            return false;

        settingMap.put(key, setting);
        return true;
    }

    public boolean remove(SimpleUri id) {
        Setting setting = get(id);

        if (setting == null || setting.hasSubscribers())
            return false;

        settingMap.remove(id);
        return true;
    }

    @SuppressWarnings("unchecked")
    public <V> Setting<V> get(SimpleUri key) {
        return (Setting<V>) settingMap.get(key);
    }

    public boolean has(SimpleUri key) {
        return settingMap.containsKey(key);
    }
}
