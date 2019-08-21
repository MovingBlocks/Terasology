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

import com.google.common.collect.Iterables;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.GrowableListWidgetBuilder;
import org.terasology.utilities.ReflectionUtil;

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
        public GrowableListArrayWidgetBuilder(
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
