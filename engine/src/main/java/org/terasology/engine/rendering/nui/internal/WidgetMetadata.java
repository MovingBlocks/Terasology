// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.internal;

import com.google.common.base.Predicate;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.UIWidget;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;

import java.lang.reflect.Field;

public class WidgetMetadata<T extends UIWidget> extends ClassMetadata<T, FieldMetadata<T, ?>> {

    /**
     * Creates a class metatdata
     *
     * @param uri                 The uri that identifies this type
     * @param type                The type to create the metadata for
     * @param factory             A reflection library to provide class construction and field get/set functionality
     * @param copyStrategyLibrary A copy strategy library
     * @throws NoSuchMethodException If the class has no default constructor
     */
    public WidgetMetadata(ResourceUrn uri, Class<T> type, ReflectFactory factory, CopyStrategyLibrary copyStrategyLibrary) throws NoSuchMethodException {
        super(uri.toString(), type, factory, copyStrategyLibrary, IsConfigField.INSTANCE);
    }

    @Override
    protected FieldMetadata<T, ?> createField(Field field, CopyStrategyLibrary copyStrategyLibrary, ReflectFactory factory) throws InaccessibleFieldException {
        return new FieldMetadata<>(this, field, copyStrategyLibrary, factory);
    }

    private static class IsConfigField implements Predicate<Field> {

        private static final IsConfigField INSTANCE = new IsConfigField();

        @Override
        public boolean apply(Field input) {
            return input.getAnnotation(LayoutConfig.class) != null;
        }
    }
}
