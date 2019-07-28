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
import com.google.common.collect.Lists;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.GrowableListWidgetFactory;
import org.terasology.utilities.ReflectionUtil;

import java.util.List;
import java.util.Optional;

public class ArrayWidgetFactory implements TypeWidgetFactory {
    private ConstructorLibrary constructorLibrary = new ConstructorLibrary();

    @Override
    public <T> Optional<UIWidget> create(Binding<T> binding, TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        if (!rawType.isArray()) {
            return Optional.empty();
        }

        if (binding.get() == null) {
            ObjectConstructor<T> constructor = constructorLibrary.get(type);
            assert constructor != null;

            binding.set(constructor.construct());
        }


        UIWidget widget = new GrowableListArrayWidgetFactory<>(
            (Binding<Object[]>) binding,
            (TypeInfo<Object[]>) type,
            library
        )
                              .create();

        return Optional.of(widget);
    }

    private static class GrowableListArrayWidgetFactory<E> extends GrowableListWidgetFactory<E[], E> {
        public GrowableListArrayWidgetFactory(
            Binding<E[]> binding,
            TypeInfo<E[]> type,
            TypeWidgetLibrary library
        ) {
            super(binding, type, ReflectionUtil.getComponentType(type), library);
        }

        @Override
        protected void updateBindingWithElements(List<E> elementList) {
            binding.set(Iterables.toArray(elementList, elementType.getRawType()));
        }

        @Override
        protected List<E> getBindingCopy() {
            return Lists.newArrayList(binding.get());
        }
    }
}
