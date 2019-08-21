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
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UITextEntry;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.util.Optional;

public abstract class NumberWidgetFactory<N extends Number> implements TypeWidgetFactory {
    private Class<N> wrapperClass;
    private Class<N> primitiveClass;

    protected NumberWidgetFactory(Class<N> wrapperClass, Class<N> primitiveClass) {
        this.wrapperClass = wrapperClass;
        this.primitiveClass = primitiveClass;
    }

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        if (!wrapperClass.equals(type.getRawType()) && !primitiveClass.equals(type.getRawType())) {
            return Optional.empty();
        }

        TypeWidgetBuilder<N> createTextEntry = this::createTextEntry;
        return Optional.of((TypeWidgetBuilder<T>) createTextEntry);
    }

    private UIWidget createTextEntry(Binding<N> numberBinding) {
        if (numberBinding.get() == null) {
            // TODO: Replace with call to guava Defaults
            setToDefaultValue(numberBinding);
        }

        UITextEntry<N> widget = new UITextEntry<>();

        widget.bindValue(numberBinding);

        widget.setParser(value -> {
            try {
                return parse(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error while parsing value ", e);
            }
        });

        return widget;
    }

    protected abstract void setToDefaultValue(Binding<N> binding);
    protected abstract N parse(String value) throws NumberFormatException;
}
