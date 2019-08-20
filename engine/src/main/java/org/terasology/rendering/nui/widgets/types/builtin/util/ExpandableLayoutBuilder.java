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

import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.NotifyingBinding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UILabel;

import static org.terasology.rendering.nui.widgets.types.TypeWidgetFactory.LABEL_WIDGET_ID;

public abstract class ExpandableLayoutBuilder<T> {
    protected final ColumnLayout mainLayout;
    protected final ColumnLayout innerExpandableLayout = createDefaultLayout();
    protected final UILabel nameWidget = new UILabel(LABEL_WIDGET_ID, "");

    protected final Binding<T> binding;

    protected ExpandableLayoutBuilder(Binding<T> binding) {
        this.binding = new NotifyingBinding<T>(binding) {
            @Override
            protected void onSet() {
                if (!innerExpandableLayout.iterator().hasNext()) {
                    // If it is empty (collapsed), we don't need to rebuild the inner layout
                    return;
                }

                clearAndPopulate(innerExpandableLayout);
            }
        };

        mainLayout = WidgetUtil.createExpandableLayout(
            nameWidget,
            () -> innerExpandableLayout,
            this::clearAndPopulate,
            this::createDefaultLayout
        );
    }

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

    private void clearAndPopulate(ColumnLayout layout) {
        layout.removeAllWidgets();

        populate(layout);
    }

    protected abstract void populate(ColumnLayout layout);
}
