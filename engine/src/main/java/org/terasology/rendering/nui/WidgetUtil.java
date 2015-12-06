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
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;

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
}
