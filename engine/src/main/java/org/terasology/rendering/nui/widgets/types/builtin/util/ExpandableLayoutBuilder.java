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
package org.terasology.rendering.nui.widgets.types.builtin.util;

import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.NotifyingBinding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;

import static org.terasology.rendering.nui.widgets.types.TypeWidgetFactory.LABEL_WIDGET_ID;

public abstract class ExpandableLayoutBuilder<T> implements TypeWidgetBuilder<T> {
    @Override
    public UIWidget build(Binding<T> binding) {
        ColumnLayout innerExpandableLayout = createDefaultLayout();
        ColumnLayout mainLayout = createDefaultLayout();

        Binding<T> wrappedBinding = new NotifyingBinding<T>(binding) {
            @Override
            protected void onSet() {
                if (!innerExpandableLayout.iterator().hasNext()) {
                    // If it is empty (collapsed), we don't need to rebuild the inner layout
                    return;
                }

                clearAndPopulate(this, innerExpandableLayout, mainLayout);
            }
        };

        WidgetUtil.createExpandableLayout(
            new UILabel(LABEL_WIDGET_ID, ""),
            () -> innerExpandableLayout,
            layout -> clearAndPopulate(wrappedBinding, layout, mainLayout),
            () -> mainLayout
        );

        postInitialize(binding, mainLayout);

        return mainLayout;
    }

    protected void postInitialize(Binding<T> binding, ColumnLayout mainLayout) {}

    protected ColumnLayout createDefaultLayout() {
        ColumnLayout layout = new ColumnLayout();

        layout.setFillVerticalSpace(false);
        layout.setAutoSizeColumns(false);
        layout.setVerticalSpacing(5);

        return layout;
    }

    protected UIBox buildErrorWidget(String errorMessage) {
        UIBox box = new UIBox();

        // TODO: Translate
        box.setContent(new UILabel(errorMessage + ", cannot instantiate object from UI"));
        return box;
    }

    private void clearAndPopulate(Binding<T> binding, ColumnLayout layout, ColumnLayout mainLayout) {
        layout.removeAllWidgets();

        populate(binding, layout, mainLayout);
    }

    protected abstract void populate(Binding<T> binding, ColumnLayout layout, ColumnLayout mainLayout);
}
