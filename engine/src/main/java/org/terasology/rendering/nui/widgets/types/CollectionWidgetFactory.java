// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.types;

import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.nui.widgets.types.builtin.util.GrowableListWidgetBuilder;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.utilities.ReflectionUtil;
import org.terasology.utilities.collection.ImmutableCollectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CollectionWidgetFactory implements TypeWidgetFactory {
    private ConstructorLibrary constructorLibrary;

    public CollectionWidgetFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        if (!Collection.class.isAssignableFrom(rawType)) {
            return Optional.empty();
        }

        TypeInfo<Collection<Object>> collectionType = (TypeInfo<Collection<Object>>) type;

        TypeWidgetBuilder<Collection<Object>> builder = new GrowableListCollectionWidgetBuilder<>(
            collectionType,
            library,
            constructorLibrary.get(collectionType)
        );

        return Optional.of((TypeWidgetBuilder<T>) builder);
    }

    private static class GrowableListCollectionWidgetBuilder<T extends Collection<E>, E>
        extends GrowableListWidgetBuilder<T, E> {

        public GrowableListCollectionWidgetBuilder(
            TypeInfo<T> type,
            TypeWidgetLibrary library,
            ObjectConstructor<T> constructor
        ) {
            super(type, ReflectionUtil.getElementType(type), library, constructor);
        }

        @Override
        protected void updateBindingWithElements(Binding<T> binding, List<E> elements) {
            try {
                binding.get().clear();
                binding.get().addAll(elements);
            } catch (UnsupportedOperationException e) {
                // Bound collection is unmodifiable, create new
                // It must either be a standard Collection or a guava ImmutableCollection
                binding.set(ImmutableCollectionUtil.copyOf(type, elements));
            }
        }

        @Override
        protected Stream<E> getBindingStream(Binding<T> binding) {
            return binding.get().stream();
        }
    }
}
