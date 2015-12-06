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

import org.terasology.engine.Time;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 */
public class UILoadBar extends CoreWidget {

    @LayoutConfig
    private TextureRegion fillTexture;

    @LayoutConfig
    private boolean animate = true;

    private Binding<Float> value = new DefaultBinding<>(0f);
    private Time time = CoreRegistry.get(Time.class);

    @Override
    public void onDraw(Canvas canvas) {
        if (fillTexture != null) {
            int size = TeraMath.floorToInt(canvas.size().x * getValue());
            int barWidth = fillTexture.getWidth();
            int offset = 0;
            if (time != null && animate) {
                offset = (int) ((time.getRealTimeInMs() / 10) % barWidth);
            }
            int drawnWidth = 0;
            // Draw Offset
            if (offset != 0) {
                int drawWidth = Math.min(size, offset);
                canvas.drawTextureRaw(fillTexture, Rect2i.createFromMinAndSize(0, 0, drawWidth, canvas.size().y)
                        , ScaleMode.STRETCH, barWidth - offset, 0, drawWidth, canvas.size().y);
                drawnWidth += drawWidth;
            }
            // Draw Remainder
            while (drawnWidth < size) {
                int drawWidth = Math.min(size - drawnWidth, barWidth);
                canvas.drawTextureRaw(fillTexture, Rect2i.createFromMinAndSize(drawnWidth, 0, drawWidth, canvas.size().y)
                        , ScaleMode.STRETCH, 0, 0, drawWidth, canvas.size().y);
                drawnWidth += drawWidth;
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return Vector2i.zero();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public TextureRegion getFillTexture() {
        return fillTexture;
    }

    public void setFillTexture(TextureRegion fillTexture) {
        this.fillTexture = fillTexture;
    }

    public boolean isAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
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
}
