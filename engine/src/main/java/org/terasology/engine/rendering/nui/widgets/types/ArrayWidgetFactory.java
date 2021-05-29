// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.types;

import com.google.common.collect.Iterables;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.nui.widgets.types.builtin.util.GrowableListWidgetBuilder;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.engine.utilities.ReflectionUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ArrayWidgetFactory implements TypeWidgetFactory {
    private ConstructorLibrary constructorLibrary;

    public ArrayWidgetFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        if (!rawType.isArray()) {
            return Optional.empty();
        }

        TypeInfo<Object[]> arrayType = (TypeInfo<Object[]>) type;

        TypeWidgetBuilder<Object[]> widget = new GrowableListArrayWidgetBuilder<>(
            arrayType,
            library,
            constructorLibrary.get(arrayType)
        );

        return Optional.of((TypeWidgetBuilder<T>) widget);
    }

    private static class GrowableListArrayWidgetBuilder<E> extends GrowableListWidgetBuilder<E[], E> {
        GrowableListArrayWidgetBuilder(
            TypeInfo<E[]> type,
            TypeWidgetLibrary library,
            ObjectConstructor<E[]> constructor
        ) {
            super(type, ReflectionUtil.getComponentType(type), library, constructor);
        }

        @Override
        protected void updateBindingWithElements(Binding<E[]> binding, List<E> elements) {
            binding.set(Iterables.toArray(elements, elementType.getRawType()));
        }

        @Override
        protected Stream<E> getBindingStream(Binding<E[]> binding) {
            return Arrays.stream(binding.get());
        }
    }
}
