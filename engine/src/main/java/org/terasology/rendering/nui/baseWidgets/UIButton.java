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

import org.terasology.input.MouseInput;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIStyle;
import org.terasology.rendering.nui.UIWidget;

/**
 * @author Immortius
 */
public class UIButton implements UIWidget {
    private String text = "";

    private UIStyle style = new UIStyle();
    private UIStyle mouseOverStyle = new UIStyle();
    private UIStyle activeStyle = new UIStyle();

    private boolean down;

    private BaseInteractionListener listener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                down = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                if (isMouseOver()) {
                    activate();
                }
                down = false;
            }
        }
    };

    public UIButton() {
    }

    public UIButton(String text, UIStyle style, UIStyle mouseOverStyle, UIStyle activeStyle) {
        this.text = text;
        this.style = style;
        this.mouseOverStyle = mouseOverStyle;
        this.activeStyle = activeStyle;
    }

    @Override
    public void draw(Canvas canvas) {
        if (down) {
            canvas.drawText(text, activeStyle);
        } else if (listener.isMouseOver()) {
            canvas.drawText(text, mouseOverStyle);
        } else {
            canvas.drawText(text, style);
        }
        canvas.addInteractionRegion(listener);
    }

    @Override
    public void update(float delta) {

    }

    private void activate() {

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

    public UIStyle getMouseOverStyle() {
        return mouseOverStyle;
    }

    public void setMouseOverStyle(UIStyle mouseOverStyle) {
        this.mouseOverStyle = mouseOverStyle;
    }

    public UIStyle getActiveStyle() {
        return activeStyle;
    }

    public void setActiveStyle(UIStyle activeStyle) {
        this.activeStyle = activeStyle;
    }
}
