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

import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author Immortius
 */
public class UIIconBar extends CoreWidget {

    @LayoutConfig
    private TextureRegion icon;

    @LayoutConfig
    private HalfIconMode halfIconMode = HalfIconMode.SPLIT;

    @LayoutConfig
    private float iconValue = 5f;

    private Binding<Float> value = new DefaultBinding<>(0f);

    @Override
    public void onDraw(Canvas canvas) {
        if (icon != null) {
            int fullIcons = TeraMath.floorToInt(getValue() / iconValue);
            int halfIcons = TeraMath.ceilToInt(getValue() / iconValue) - fullIcons;
            Vector2i offset = new Vector2i();
            for (int i = 0; i < fullIcons; ++i) {
                canvas.drawTexture(icon, Rect2i.createFromMinAndSize(offset, icon.size()));
                offset.x += icon.getWidth();
                if (offset.x + icon.getWidth() > canvas.size().x) {
                    offset.x = 0;
                    offset.y += icon.getHeight();
                }
            }
            for (int i = 0; i < halfIcons; ++i) {
                switch (halfIconMode) {
                    case SHRINK:
                        Vector2i halfSize = new Vector2i(icon.size());
                        halfSize.x /= 2;
                        halfSize.y /= 2;
                        canvas.drawTexture(icon, Rect2i.createFromMinAndSize(new Vector2i(offset.x + halfSize.x, offset.y + halfSize.y), halfSize));
                        break;
                    case SPLIT:
                        canvas.drawTextureRaw(icon, Rect2i.createFromMinAndSize(offset, new Vector2i(icon.getWidth() / 2, icon.getHeight())),
                                ScaleMode.STRETCH, 0f, 0f, 0.5f, 1.0f);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (icon != null) {
            int icons = TeraMath.ceilToInt(getValue() / iconValue);
            int maxHorizontalIcons = TeraMath.floorToInt(sizeHint.x / icon.getWidth());
            int rows = ((icons - 1) / maxHorizontalIcons) + 1;
            return new Vector2i(Math.min(icons, maxHorizontalIcons) * icon.getWidth(), rows * icon.getHeight());
        } else {
            return Vector2i.zero();
        }
    }

    public void bindValue(Binding<Float> binding) {
        value = binding;
    }

    public float getValue() {
        return value.get();
    }

    public void setValue(float val) {
        value.set(val);
    }

    public TextureRegion getIcon() {
        return icon;
    }

    public void setIcon(TextureRegion icon) {
        this.icon = icon;
    }

    public HalfIconMode getHalfIconMode() {
        return halfIconMode;
    }

    public void setHalfIconMode(HalfIconMode halfIconMode) {
        this.halfIconMode = halfIconMode;
    }

    public float getIconValue() {
        return iconValue;
    }

    public void setIconValue(float iconValue) {
        this.iconValue = iconValue;
    }

    public enum HalfIconMode {
        NONE,
        SPLIT,
        SHRINK,
    }
}
