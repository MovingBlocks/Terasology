// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.metadata;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

import javax.inject.Inject;

/**
 * The set of metadata libraries used by the entity system
 *
 */
public class EntitySystemLibrary {

    private final TypeHandlerLibrary typeHandlerLibrary;
    private final ComponentLibrary componentLibrary;
    private final EventLibrary eventLibrary;

    @Inject
    public EntitySystemLibrary(TypeHandlerLibrary typeHandlerLibrary, ComponentLibrary componentLibrary, EventLibrary eventLibrary) {
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.componentLibrary = componentLibrary;
        this.eventLibrary = eventLibrary;
    }

    public EntitySystemLibrary(Context context, TypeHandlerLibrary typeHandlerLibrary) {
        this(typeHandlerLibrary,
                new ComponentLibrary(context.get(ModuleManager.class) != null ? context.get(ModuleManager.class).getEnvironment() : null,
                        context.get(ReflectFactory.class),
                        context.get(CopyStrategyLibrary.class)),
                new EventLibrary(context.get(ModuleManager.class) != null ? context.get(ModuleManager.class).getEnvironment() : null,
                        context.get(ReflectFactory.class),
                        context.get(CopyStrategyLibrary.class)));
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
