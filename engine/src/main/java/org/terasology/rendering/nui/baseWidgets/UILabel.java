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
package org.terasology.rendering.nui.baseWidgets;

import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIStyle;
import org.terasology.rendering.nui.UIWidget;

/**
 * @author Immortius
 */
public class UILabel implements UIWidget {

    private String text = "";
    private UIStyle style = new UIStyle();

    public UILabel() {
    }

    public UILabel(String text) {
        this.text = text;
    }

    public UILabel(String text, UIStyle style) {
        this.text = text;
        this.style = style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UIStyle getStyle() {
        return style;
    }

    public void setStyle(UIStyle style) {
        this.style = style;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(text, style);
    }

    @Override
    public void update(float delta) {
    }
}
