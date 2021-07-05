// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

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
