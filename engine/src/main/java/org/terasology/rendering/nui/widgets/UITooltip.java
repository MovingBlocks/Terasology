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
package org.terasology.rendering.nui.widgets;

import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.databinding.Binding;

/**
 * @author Immortius
 */
public class UITooltip extends CursorAttachment {

    private UILabel label;

    public UITooltip() {
        label = new UILabel();
        setAttachment(label);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!getText().isEmpty()) {
            super.onDraw(canvas);
        }
    }

    public void bindBinding(Binding<String> binding) {
        label.bindText(binding);
    }

    public String getText() {
        return label.getText();
    }

    public void setText(String val) {
        label.setText(val);
    }
}
