/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISpace;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public final class WidgetUtil {
    public static final int INDENT_SIZE = 50;

    private static final Logger logger = LoggerFactory.getLogger(WidgetUtil.class);

    private WidgetUtil() {
    }

    public static void trySubscribe(UIWidget widget, String id, ActivateEventListener listener) {
        UIButton button = widget.find(id, UIButton.class);
        if (button != null) {
            button.subscribe(listener);
        } else {
            logger.warn("Contents of {} missing button with id '{}'", widget, id);
        }
    }

    public static void tryBindCheckbox(UIWidget widget, String id, Binding<Boolean> binding) {
        UICheckbox checkbox = widget.find(id, UICheckbox.class);
        if (checkbox != null) {
            checkbox.bindChecked(binding);
        }
    }

    /**
     * Bind a check box and boolean, and a listener will be subscribed to the checkbox.
     *
     * @param widget   the widget.
     * @param id       the id of the checkbox.
     * @param binding  the boolean bound with the checkbox.
     * @param listener the listener which will activated when the check box is pressed.
     */
    public static void tryBindCheckBoxWithListener(UIWidget widget, String id, Binding<Boolean> binding, ActivateEventListener listener) {
        UICheckbox checkbox = widget.find(id, UICheckbox.class);
        if (checkbox != null) {
            checkbox.bindChecked(binding);
            checkbox.subscribe(listener);
        }
    }

    public static UIWidget indent(UIWidget widget) {
        RowLayout layout = new RowLayout();

        layout.addWidget(new UISpace(new Vector2i(INDENT_SIZE, 0)), new RowLayoutHint().setUseContentWidth(true));
        layout.addWidget(widget, new RowLayoutHint());

        return layout;
    }

    public static <L extends UILayout<?>> ColumnLayout createExpandableLayout(
        String label,
        Supplier<L> layoutSupplier,
        Consumer<L> layoutExpander
    ) {
        return createExpandableLayout(new UILabel(label), layoutSupplier, layoutExpander, ColumnLayout::new);
    }

    public static <L extends UILayout<?>> ColumnLayout createExpandableLayout(
        String label, Supplier<L> layoutSupplier,
        Consumer<L> layoutExpander,
        Supplier<ColumnLayout> columnLayoutSupplier
    ) {
        return createExpandableLayout(new UILabel(label), layoutSupplier, layoutExpander, columnLayoutSupplier);
    }

    public static <L extends UILayout<?>> ColumnLayout createExpandableLayout(
        UILabel labelWidget,
        Supplier<L> layoutSupplier,
        Consumer<L> layoutExpander,
        Supplier<ColumnLayout> columnLayoutSupplier
    ) {
        L layoutToExpand = layoutSupplier.get();

        RowLayout expanderLayout = createExpanderWidget(labelWidget, layoutToExpand, layoutExpander);

        ColumnLayout columnLayout = columnLayoutSupplier.get();

        columnLayout.addWidget(expanderLayout);
        columnLayout.addWidget(indent(layoutToExpand));

        return columnLayout;
    }

    public static <L extends UILayout<?>> RowLayout createExpanderWidget(
        String label,
        L layoutToExpand,
        Consumer<L> layoutExpander
    ) {
        return createExpanderWidget(new UILabel(label), layoutToExpand, layoutExpander);
    }

    public static <L extends UILayout<?>> RowLayout createExpanderWidget(
        UILabel labelWidget,
        L layoutToExpand,
        Consumer<L> layoutExpander
    ) {
        RowLayout rowLayout = new RowLayout();

        UIButton expandButton = createExpanderButton(layoutToExpand, layoutExpander);

        rowLayout.addWidget(expandButton, new RowLayoutHint().setUseContentWidth(true));
        rowLayout.addWidget(labelWidget, new RowLayoutHint());
        return rowLayout;
    }

    public static <L extends UILayout<?>> UIButton createExpanderButton(L layoutToExpand,
                                                                        Consumer<L> layoutExpander) {
        UIButton expandButton = new UIButton();

        expandButton.setText("+");
        expandButton.subscribe(widget -> {
            UIButton button = (UIButton) widget;
            if ("-".equals(button.getText())) {
                layoutToExpand.removeAllWidgets();

                button.setText("+");
                // TODO: Translate
                button.setTooltip("Expand");
            } else {
                layoutExpander.accept(layoutToExpand);

                button.setText("-");
                // TODO: Translate
                button.setTooltip("Collapse");
            }
        });

        return expandButton;
    }

    public static UIWidget labelize(UIWidget widget, String labelText, String labelWidgetId) {
        Optional<UILabel> labelWidget = widget.tryFind(labelWidgetId, UILabel.class);

        if (labelWidget.isPresent()) {
            labelWidget.get().setText(labelText);

            return widget;
        }

        RowLayout fieldLayout = new RowLayout();
        fieldLayout.setHorizontalSpacing(5);

        fieldLayout.addWidget(new UILabel(labelWidgetId, labelText), new RowLayoutHint().setUseContentWidth(true));
        fieldLayout.addWidget(widget, new RowLayoutHint());

        return fieldLayout;
    }
}
