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
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.FieldsWidgetBuilder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class LabeledNumberRowLayoutBuilder<N extends Number> {
    public static final String LABEL_ID = "_numberRowLayoutLabel";
    private static final Logger LOGGER = LoggerFactory.getLogger(LabeledNumberRowLayoutBuilder.class);

    private final List<Binding<N>> bindings = Lists.newArrayList();
    private final List<String> labels = Lists.newArrayList();
    private final Class<N> clazz;
    private final TypeWidgetLibrary library;

    public LabeledNumberRowLayoutBuilder(Class<N> clazz, TypeWidgetLibrary library) {
        this.clazz = clazz;
        this.library = library;
    }

    public LabeledNumberRowLayoutBuilder<N> addField(String fieldName, Binding<?> parent) {
        Class<?> parentClass = parent.get().getClass();
        Field declaredField;

        try {
            declaredField = parentClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Could not find field {} in type {}", fieldName, parentClass.getName());
            return this;
        }


        Optional<Binding<N>> fieldBinding = FieldsWidgetBuilder.getFieldBinding(parent, declaredField);

        if (!fieldBinding.isPresent()) {
            LOGGER.error("Could not bind field {} in type {}", fieldName, parentClass.getName());
            return this;
        }

        labels.add(fieldName);
        bindings.add(fieldBinding.get());

        return this;
    }

    public UIWidget build() {
        int count = labels.size();
        assert bindings.size() == count;

        float relativeWidth = 1.0f / count;

        RowLayout rowLayout = new RowLayout();
        rowLayout.setHorizontalSpacing(5);

        for (int i = 0; i < count; i++) {
            Binding<N> binding = bindings.get(i);
            String label = labels.get(i);

            Optional<UIWidget> widget = library.getWidget(binding, clazz);

            if (!widget.isPresent()) {
                LOGGER.error("Could not create widget for numeric type {}", clazz.getSimpleName());
                return rowLayout;
            }

            UIWidget labelized = WidgetUtil.labelize(widget.get(), label, LABEL_ID);
            rowLayout.addWidget(labelized, new RowLayoutHint(relativeWidth));
        }

        return rowLayout;
    }
}
