/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.widgets;

import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UIStyle;

import java.util.List;

public class TooltipLineRenderer extends AbstractItemRenderer<TooltipLine> {
    private UISkin defaultSkin;

    public TooltipLineRenderer(UISkin defaultSkin) {
        this.defaultSkin = defaultSkin;
    }

    @Override
    public void draw(TooltipLine value, Canvas canvas) {
        final UISkin skin = value.getSkin();
        if (skin != null) {
            canvas.setSkin(skin);
            canvas.setFamily(value.getFamily());
            canvas.drawText(value.getText());
        } else {
            canvas.setSkin(defaultSkin);

            Font font = value.getFont();
            if (font == null) {
                font = canvas.getCurrentStyle().getFont();
            }
            Color color = value.getColor();
            if (color == null) {
                color = canvas.getCurrentStyle().getTextColor();
            }
            canvas.drawTextRaw(value.getText(), font, color, canvas.getRegion());
        }
    }

    @Override
    public Vector2i getPreferredSize(TooltipLine value, Canvas canvas) {
        UISkin skin = value.getSkin();
        if (skin == null) {
            skin = defaultSkin;
        }
        final UIStyle style = skin.getStyleFor(value.getFamily(), UIList.class, "item", UIWidget.DEFAULT_MODE);
        Font font = style.getFont();
        List<String> lines = TextLineBuilder.getLines(font, value.getText(), canvas.size().x);
        return font.getSize(lines);
    }
}
