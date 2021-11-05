// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import org.terasology.engine.utilities.ReflectionUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.nui.widgets.types.builtin.util.GrowableListWidgetBuilder;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
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

        GrowableListCollectionWidgetBuilder(
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
                Class<T> rawType = type.getRawType();
                Iterable<E> filtered = Iterables.filter(elements, Objects::nonNull);
                if (SortedSet.class.isAssignableFrom(rawType)) {
                    binding.set((T) ImmutableSortedSet.copyOf(filtered));
                }
                if (Set.class.isAssignableFrom(rawType)) {
                    binding.set((T) ImmutableSet.copyOf(filtered));
                }
                binding.set((T) ImmutableList.copyOf(filtered));
            }
        }

        @Override
        protected Stream<E> getBindingStream(Binding<T> binding) {
            return binding.get().stream();
        }
    }
}
