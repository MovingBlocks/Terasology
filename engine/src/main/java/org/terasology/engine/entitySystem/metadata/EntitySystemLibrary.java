// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.metadata;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 * The set of metadata libraries used by the entity system
 *
 */
public class EntitySystemLibrary {

    private final TypeHandlerLibrary typeHandlerLibrary;
    private final ComponentLibrary componentLibrary;
    private final EventLibrary eventLibrary;

    public EntitySystemLibrary(Context context, TypeHandlerLibrary typeHandlerLibrary) {
        // NOTE: Work-around to fix tests
        ModuleManager manager = context.get(ModuleManager.class);
        ModuleEnvironment environment = null;
        if (manager != null) {
            environment = manager.getEnvironment();
        }
        ReflectFactory reflectFactory = context.get(ReflectFactory.class);
        CopyStrategyLibrary copyStrategyLibrary = context.get(CopyStrategyLibrary.class);

        this.typeHandlerLibrary = typeHandlerLibrary;
        this.componentLibrary = new ComponentLibrary(environment, reflectFactory, copyStrategyLibrary);
        this.eventLibrary = new EventLibrary(environment, reflectFactory, copyStrategyLibrary);
    }

    /**
     * @return The library of component metadata
     */
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    /**
     * @return The library of serializers
     */
    public TypeHandlerLibrary getSerializationLibrary() {
        return typeHandlerLibrary;
    }

    /**
     * @return The library of event metadata
     */
    public EventLibrary getEventLibrary() {
        return eventLibrary;
    }

}
