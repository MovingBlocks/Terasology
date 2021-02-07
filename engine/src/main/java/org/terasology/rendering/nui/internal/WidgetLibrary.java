// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.module.ModuleEnvironment;
import org.terasology.nui.UIWidget;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.AbstractClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 */
public class WidgetLibrary extends AbstractClassLibrary<UIWidget> {

    private static final Logger logger = LoggerFactory.getLogger(WidgetLibrary.class);

    public WidgetLibrary(ModuleEnvironment environment, ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategyLibrary) {
        super(environment, reflectFactory, copyStrategyLibrary);
    }

    @Override
    protected <C extends UIWidget> ClassMetadata<C, ?> createMetadata(Class<C> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, ResourceUrn name) {
        try {
            return new WidgetMetadata<>(name, type, factory, copyStrategies);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;

        }
    }
}
