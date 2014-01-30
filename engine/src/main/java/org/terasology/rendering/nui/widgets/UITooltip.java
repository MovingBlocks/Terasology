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

import org.terasology.input.Mouse;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.skin.UIStyle;

/**
 * @author Immortius
 */
public class UITooltip extends UILabel {

    private static final int MOUSE_CURSOR_HEIGHT = 18;

    @Override
    public void onDraw(Canvas canvas) {
        if (getText().isEmpty()) {
            return;
        }

        UIStyle style = canvas.getCurrentStyle();
        Vector2i textSize = new Vector2i(style.getFont().getWidth(getText()), style.getFont().getHeight(getText()));
        textSize.add(style.getMargin().getTotals());

        int top;
        switch (style.getVerticalAlignment()) {
            case TOP:
                top = Mouse.getPosition().y - textSize.y;
                break;
            case MIDDLE:
                top = Mouse.getPosition().y;
                break;
            default:
                top = Mouse.getPosition().y + MOUSE_CURSOR_HEIGHT;
                break;
        }
        top = TeraMath.clamp(top, 0, canvas.size().y - textSize.y);
        int left;
        switch (style.getHorizontalAlignment()) {
            case RIGHT:
                left = Mouse.getPosition().x - textSize.x;
                break;
            case CENTER:
                left = Mouse.getPosition().x - textSize.x / 2;
                break;
            default:
                left = Mouse.getPosition().x;
                break;
        }
        left = TeraMath.clamp(left, 0, canvas.size().x - textSize.x);

        try (SubRegion ignored = canvas.subRegion(Rect2i.createFromMinAndSize(left, top, textSize.x, textSize.y), false)) {
            canvas.drawBackground();
            super.onDraw(canvas);
        }
    }

    @Override
    public boolean isSkinAppliedByCanvas() {
        return false;
    }
}
