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

import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.GrowableListWidgetBuilder;
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
