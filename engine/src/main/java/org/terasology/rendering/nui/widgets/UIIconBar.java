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

import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.skin.UIStyle;

/**
 */
public class UIIconBar extends CoreWidget {

    private static final String ICON_PART = "icon";

    @LayoutConfig
    private HalfIconMode halfIconMode = HalfIconMode.SPLIT;

    @LayoutConfig
    private int maxIcons = 10;

    @LayoutConfig
    private int spacing = 1;

    @LayoutConfig
    private TextureRegion icon;

    private Binding<Float> value = new DefaultBinding<>(0f);
    private Binding<Float> maxValue = new DefaultBinding<>(10f);

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart(ICON_PART);
        if (icon != null && getMaxValue() > 0) {
            Vector2i iconSize = getIconSize(canvas);
            float ratio = maxIcons * getValue() / getMaxValue();
            int fullIcons = TeraMath.floorToInt(ratio);
            boolean halfIcon = false;
            if (ratio - fullIcons >= 0.5f) {
                fullIcons++;
            } else if (ratio - fullIcons > 0) {
                halfIcon = true;
            }
            Vector2i offset = new Vector2i();
            for (int i = 0; i < maxIcons; ++i) {
                Rect2i iconRegion = Rect2i.createFromMinAndSize(offset, iconSize);
                canvas.drawBackground(iconRegion);
                if (ratio - i >= 0.5f) {
                    canvas.drawTexture(icon, iconRegion);
                } else if (ratio - i > 0f) {
                    switch (halfIconMode) {
                        case SHRINK:
                            Vector2i halfSize = new Vector2i(iconSize);
                            halfSize.x /= 2;
                            halfSize.y /= 2;
                            canvas.drawTexture(icon,
                                    Rect2i.createFromMinAndSize(new Vector2i(offset.x + halfSize.x / 2, offset.y + halfSize.y / 2), halfSize));
                            break;
                        case SPLIT:
                            canvas.drawTextureRaw(icon,
                                    Rect2i.createFromMinAndSize(offset, new Vector2i(iconSize.x / 2, iconSize.y)),
                                    ScaleMode.STRETCH, 0f, 0f, (float) (iconSize.x / 2) / iconSize.x, 1.0f);
                            break;
                        default:
                            canvas.drawTexture(icon, iconRegion);
                            break;
                    }
                }
                offset.x += iconSize.x + spacing;
                if (offset.x + iconSize.x > canvas.size().x) {
                    offset.x = 0;
                    offset.y += iconSize.y + spacing;
                }
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(ICON_PART);
        if (icon != null) {
            Vector2i iconSize = getIconSize(canvas);
            int maxHorizontalIcons = sizeHint.x / iconSize.x;
            int rows = ((maxIcons - 1) / maxHorizontalIcons) + 1;
            int columns = Math.min(maxIcons, maxHorizontalIcons);
            return new Vector2i(columns * iconSize.x + (columns - 1) * spacing, rows * iconSize.y + (rows - 1) * spacing);
        } else {
            return Vector2i.zero();
        }

    }

    private Vector2i getIconSize(Canvas canvas) {
        UIStyle iconStyle = canvas.getCurrentStyle();
        int width = iconStyle.getFixedWidth();
        int height = iconStyle.getFixedHeight();
        if (width == 0) {
            width = iconStyle.getMinWidth();
        }
        if (height == 0) {
            height = iconStyle.getMinHeight();
        }
        if (width == 0) {
            width = icon.getWidth();
        }
        if (height == 0) {
            height = icon.getHeight();
        }
        return new Vector2i(width, height);
    }

    public TextureRegion getIcon() {
        return icon;
    }

    public void setIcon(TextureRegion icon) {
        this.icon = icon;
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

    public void bindMaxValue(Binding<Float> binding) {
        maxValue = binding;
    }

    public float getMaxValue() {
        return maxValue.get();
    }

    public void setMaxValue(float val) {
        maxValue.set(val);
    }

    public HalfIconMode getHalfIconMode() {
        return halfIconMode;
    }

    public void setHalfIconMode(HalfIconMode halfIconMode) {
        this.halfIconMode = halfIconMode;
    }

    public enum HalfIconMode {
        NONE,
        SPLIT,
        SHRINK,
    }


}
