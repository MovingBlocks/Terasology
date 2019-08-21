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
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.util.Optional;

public class BooleanWidgetFactory implements TypeWidgetFactory {
    @SuppressWarnings({"unchecked"})
    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        if (!Boolean.class.equals(type.getRawType()) && !Boolean.TYPE.equals(type.getRawType())) {
            return Optional.empty();
        }

        TypeWidgetBuilder<Boolean> createBooleanWidget = BooleanWidgetFactory::createBooleanWidget;
        return Optional.of((TypeWidgetBuilder<T>) createBooleanWidget);
    }

    private static UIWidget createBooleanWidget(Binding<Boolean> binding) {
        if (binding.get() == null) {
            binding.set(false);
        }

        UICheckbox widget = new UICheckbox();

        widget.bindChecked(binding);

        return widget;
    }
}
