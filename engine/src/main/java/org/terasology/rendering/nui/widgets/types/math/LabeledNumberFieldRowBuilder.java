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
package org.terasology.rendering.nui.widgets.types.math;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.FieldsWidgetBuilder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class LabeledNumberFieldRowBuilder<T, N extends Number> implements TypeWidgetBuilder<T> {
    public static final String LABEL_ID = "_numberRowLayoutLabel";
    private static final Logger LOGGER = LoggerFactory.getLogger(LabeledNumberFieldRowBuilder.class);

    private final List<Function<Binding<T>, Binding<N>>> itemBindingGenerators = Lists.newArrayList();
    private final List<String> labels = Lists.newArrayList();
    private final Class<T> clazz;
    private final Class<N> numberClass;
    private final TypeWidgetLibrary library;

    public LabeledNumberFieldRowBuilder(Class<T> clazz, Class<N> numberClass, TypeWidgetLibrary library) {
        this.clazz = clazz;
        this.numberClass = numberClass;
        this.library = library;
    }

    public LabeledNumberFieldRowBuilder<T, N> addAllFields() {
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (!declaredField.getType().equals(numberClass)) {
                continue;
            }

            String fieldName = declaredField.getName();

            Function<Binding<T>, Binding<N>> generator = parent -> {
                Optional<Binding<N>> fieldBinding = FieldsWidgetBuilder.getFieldBinding(parent, declaredField);

                if (!fieldBinding.isPresent()) {
                    LOGGER.error("Could not bind field {} in type {}", fieldName, clazz.getName());
                    return null;
                }

                return fieldBinding.get();
            };

            add(fieldName, generator);
        }

        return this;
    }

    public LabeledNumberFieldRowBuilder<T, N> add(String label, Function<Binding<T>, Binding<N>> binding) {
        labels.add(label);
        itemBindingGenerators.add(binding);

        return this;
    }

    @Override
    public UIWidget build(Binding<T> binding) {
        if (binding.get() == null) {
            binding.set(getDefaultValue());
        }

        int count = labels.size();
        assert itemBindingGenerators.size() == count;

        float relativeWidth = 1.0f / count;

        RowLayout rowLayout = new RowLayout();
        rowLayout.setHorizontalSpacing(5);

        for (int i = 0; i < count; i++) {
            Binding<N> itemBinding = itemBindingGenerators.get(i).apply(binding);

            if (itemBinding == null) {
                continue;
            }

            String label = labels.get(i);

            Optional<UIWidget> widget = library.getWidget(itemBinding, numberClass);

            if (!widget.isPresent()) {
                LOGGER.error("Could not create widget for numeric type {}", numberClass.getSimpleName());
                return rowLayout;
            }

            UIWidget labelized = WidgetUtil.labelize(widget.get(), label, LABEL_ID);
            rowLayout.addWidget(labelized, new RowLayoutHint(relativeWidth));
        }

        return rowLayout;
    }

    protected abstract T getDefaultValue();
}
