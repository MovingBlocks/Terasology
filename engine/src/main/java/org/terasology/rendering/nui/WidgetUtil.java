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
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UILabel;

import java.util.function.Consumer;

/**
 */
public final class WidgetUtil {

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
     * @param widget the widget.
     * @param id the id of the checkbox.
     * @param binding the boolean bound with the checkbox.
     * @param listener the listener which will activated when the check box is pressed.
     */
    public static void tryBindCheckBoxWithListener(UIWidget widget, String id, Binding<Boolean> binding, ActivateEventListener listener) {
        UICheckbox checkbox = widget.find(id, UICheckbox.class);
        if (checkbox != null) {
            checkbox.bindChecked(binding);
            checkbox.subscribe(listener);
        }
    }

    public static <L extends UILayout<?>> RowLayout createExpanderLayout(String label,
                                                                         L layoutToExpand,
                                                                         Consumer<L> layoutExpander) {
        RowLayout rowLayout = new RowLayout();

        UIButton expandButton = createExpanderButton(layoutToExpand, layoutExpander);

        rowLayout.addWidget(expandButton, new RowLayoutHint().setUseContentWidth(true));
        rowLayout.addWidget(new UILabel(label), new RowLayoutHint());
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
}
