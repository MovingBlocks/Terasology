/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.engine.module;

import org.terasology.gestalt.module.Module;

/**
 * A set of extra module extensions.
 */
public enum ExtraDataModuleExtension implements ModuleExtension {

    AUTHOR("author", String.class),
    ORIGIN("origin", String.class);

    private final String key;
    private final Class<?> valueType;

    ExtraDataModuleExtension(String key, Class<?> valueType) {
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

    public static String getOrigin(Module module) {
        final String origin = module.getMetadata().getExtension(ORIGIN.getKey(), String.class);
        return origin != null ? origin : "";
    }

    public static String getAuthor(Module module) {
        final String author = module.getMetadata().getExtension(AUTHOR.getKey(), String.class);
        return author != null ? author : "";
    }

}
