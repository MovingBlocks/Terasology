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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;

import java.util.Map;

public class FlexibleConfigImpl implements FlexibleConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexibleConfigImpl.class);

    private Map<SimpleUri, Setting> settingMap;

    public FlexibleConfigImpl() {
        this.settingMap = Maps.newHashMap();
    }

    public <V> boolean add(Setting<V> setting) {
        SimpleUri id = setting.getId();

        if (id == null) {
            LOGGER.warn("The id of a setting cannot be null.");
            return false;
        } else if (contains(id)) {
            LOGGER.warn("A Setting with the id \"{}\" already exists.", id);
            return false;
        }

        settingMap.put(id, setting);
        return true;
    }

    public boolean remove(SimpleUri id) {
        Setting setting = get(id);

        if (setting == null) {
            LOGGER.warn("Setting \"{}\" does not exist.", id);
            return false;
        } else if (setting.hasSubscribers()) {
            LOGGER.warn("Setting \"{}\" cannot be removed while it has subscribers.", id);
            return false;
        }

        settingMap.remove(id);
        return true;
    }

    @SuppressWarnings("unchecked")
    public <V> Setting<V> get(SimpleUri id) {
        return (Setting<V>) settingMap.get(id);
    }

    public boolean contains(SimpleUri id) {
        return settingMap.containsKey(id);
    }
}
