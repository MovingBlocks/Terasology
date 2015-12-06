/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.module;

import org.terasology.engine.SimpleUri;
import org.terasology.module.Module;

/**
 * A set of standard module extensions.
 */
public enum StandardModuleExtension implements ModuleExtension {

    SERVER_SIDE_ONLY("serverSideOnly", Boolean.class),
    IS_GAMEPLAY("isGameplay", Boolean.class),
    DEFAULT_WORLD_GENERATOR("defaultWorldGenerator", String.class);

    private final String key;
    private final Class<?> valueType;

    private StandardModuleExtension(String key, Class<?> valueType) {
        this.key = key;
        this.valueType = valueType;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Class<?> getValueType() {
        return valueType;
    }

    public static boolean isServerSideOnly(Module module) {
        Boolean serverSideOnly = module.getMetadata().getExtension(SERVER_SIDE_ONLY.getKey(), Boolean.class);
        return serverSideOnly != null && serverSideOnly;
    }

    public static boolean isGameplayModule(Module module) {
        Boolean isGameplay = module.getMetadata().getExtension(IS_GAMEPLAY.getKey(), Boolean.class);
        return isGameplay != null && isGameplay;
    }

    public static SimpleUri getDefaultWorldGenerator(Module module) {
        String ext = module.getMetadata().getExtension(DEFAULT_WORLD_GENERATOR.getKey(), String.class);
        return ext != null ? new SimpleUri(ext) : null;
    }
}
