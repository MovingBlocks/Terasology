// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.SimpleUri;
import org.terasology.gestalt.module.Module;

import java.util.EnumSet;
import java.util.Set;

/**
 * A set of standard module extensions.
 */
public enum StandardModuleExtension implements ModuleExtension {

    SERVER_SIDE_ONLY("serverSideOnly", Boolean.class),
    IS_GAMEPLAY("isGameplay", Boolean.class),
    IS_ASSET("isAsset", Boolean.class),
    IS_WORLD("isWorld", Boolean.class),
    IS_LIBRARY("isLibrary", Boolean.class),
    IS_SPECIAL("isSpecial", Boolean.class),
    IS_AUGMENTATION("isAugmentation", Boolean.class),
    DEFAULT_WORLD_GENERATOR("defaultWorldGenerator", String.class);

    private final String key;
    private final Class<?> valueType;
    private static final Logger logger = LoggerFactory.getLogger(StandardModuleExtension.class);

    StandardModuleExtension(String key, Class<?> valueType) {
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
        return getBooleanExtension(module, SERVER_SIDE_ONLY);
    }

    public static boolean isGameplayModule(Module module) {
        return getBooleanExtension(module, IS_GAMEPLAY);
    }

    public static SimpleUri getDefaultWorldGenerator(Module module) {
        String ext = module.getMetadata().getExtension(DEFAULT_WORLD_GENERATOR.getKey(), String.class);
        return ext != null ? new SimpleUri(ext) : null;
    }

    private static boolean getBooleanExtension(Module module, StandardModuleExtension ext) {
        Boolean result = module.getMetadata().getExtension(ext.getKey(), Boolean.class);
        return result != null && result;
    }

    public static Set<StandardModuleExtension> booleanPropertySet() {
        Set<StandardModuleExtension> booleanPropertySet = EnumSet.noneOf(StandardModuleExtension.class);
        for (StandardModuleExtension standardModuleExtension : values()) {
            if (standardModuleExtension.getValueType().equals(Boolean.class)) {
                booleanPropertySet.add(standardModuleExtension);
            }
        }
        return booleanPropertySet;
    }

    public boolean isProvidedBy(Module module) {
        return getBooleanExtension(module, this);
    }
}
