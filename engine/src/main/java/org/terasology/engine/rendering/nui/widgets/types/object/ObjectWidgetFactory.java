// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.types.object;

import org.terasology.engine.core.module.ModuleManager;
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;
import org.terasology.engine.registry.In;

import java.lang.reflect.Modifier;
import java.util.Optional;

public class ObjectWidgetFactory implements TypeWidgetFactory {
    @In
    private ModuleManager moduleManager;

    @In
    private TypeRegistry typeRegistry;

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        // If the class is a local class or a non-static member class, we don't want to handle it
        // We do not handle the object class either because there are too many types to deal with
        if (rawType.isLocalClass() || Object.class.equals(rawType) ||
                (rawType.isMemberClass() && !Modifier.isStatic(rawType.getModifiers()))) {
            return Optional.empty();
        }

        ObjectLayoutBuilder<T> layoutBuilder = new ObjectLayoutBuilder<>(type, library);

        return Optional.of(layoutBuilder);
    }

}
