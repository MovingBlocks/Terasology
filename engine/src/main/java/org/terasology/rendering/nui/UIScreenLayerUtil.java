/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UICheckbox;
import org.terasology.rendering.nui.databinding.Binding;

/**
 * @author Immortius
 */
public final class UIScreenLayerUtil {

    private static final Logger logger = LoggerFactory.getLogger(UIScreenLayerUtil.class);

    private UIScreenLayerUtil() {
    }

    public static void trySubscribe(UIScreenLayer screen, String id, ButtonEventListener listener) {
        UIButton button = screen.find(id, UIButton.class);
        if (button != null) {
            button.subscribe(listener);
        } else {
            logger.warn("Contents of {} missing button with id '{}'", screen, id);
        }
    }

    public static void tryBindCheckbox(UIScreenLayer screen, String id, Binding<Boolean> binding) {
        UICheckbox checkbox = screen.find(id, UICheckbox.class);
        if (checkbox != null) {
            checkbox.bindChecked(binding);
        }
    }
}
