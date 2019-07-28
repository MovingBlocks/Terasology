/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.widgets.types.builtin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.GrowableListWidgetFactory;
import org.terasology.utilities.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public class CollectionWidgetFactory implements TypeWidgetFactory {
    private ConstructorLibrary constructorLibrary;

    public CollectionWidgetFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<UIWidget> create(Binding<T> binding, TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        if (!Collection.class.isAssignableFrom(rawType)) {
            return Optional.empty();
        }

        if (binding.get() == null) {
            ObjectConstructor<T> constructor = constructorLibrary.get(type);
            assert constructor != null;

            binding.set(constructor.construct());
        }

        UIWidget widget = new GrowableListCollectionWidgetFactory<>(
            (Binding<Collection<Object>>) binding,
            (TypeInfo<Collection<Object>>) type,
            library
        )
                              .create();

        return Optional.of(widget);
    }

    private static class GrowableListCollectionWidgetFactory<T extends Collection<E>, E>
        extends GrowableListWidgetFactory<T, E> {

        public GrowableListCollectionWidgetFactory(
            Binding<T> binding,
            TypeInfo<T> type,
            TypeWidgetLibrary library
        ) {
            super(binding, type, ReflectionUtil.getElementType(type), library);
        }

        private T newImmutableCollection(TypeInfo<T> type, Collection<E> items) {
            Class<T> rawType = type.getRawType();

            // If the bound collection is unmodifiable, it must either be a standard
            // Collection or a guava ImmutableCollection, so casts always succeed

            // TODO: Support more Guava types?

            if (SortedSet.class.isAssignableFrom(rawType)) {
                return (T) ImmutableSortedSet.copyOf(items);
            }

            if (Set.class.isAssignableFrom(rawType)) {
                return (T) ImmutableSet.copyOf(items);
            }

            return (T) ImmutableList.copyOf(items);
        }

        @Override
        protected void updateBindingWithElements(List<E> elementList) {
            try {
                binding.get().clear();
                binding.get().addAll(elementList);
            } catch (UnsupportedOperationException e) {
                // Bound collection is unmodifiable, create new
                binding.set(newImmutableCollection(type, elementList));
            }
        }

        @Override
        protected List<E> getBindingCopy() {
            return new ArrayList<>(binding.get());
        }
    }
}
