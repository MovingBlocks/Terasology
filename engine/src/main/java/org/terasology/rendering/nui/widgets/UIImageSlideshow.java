/*
 * Copyright 2018 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A widget to display image slideshows
 */
public class UIImageSlideshow extends CoreWidget {

    private Logger logger = LoggerFactory.getLogger(UIImageSlideshow.class);

    private Binding<TextureRegion> currentImage = new DefaultBinding<>();

    private long timestamp = new Date().getTime();
    private boolean active = true;

    @LayoutConfig
    private int index = 0;

    @LayoutConfig
    private int speed = 5; // seconds

    @LayoutConfig
    private boolean infinite = true;

    @LayoutConfig
    private boolean ignoreAspectRatio;


    private List<TextureRegion> images = new ArrayList<>();

    @Override
    public void onDraw(Canvas canvas) {
        if (currentImage.get() != null) {
            canvas.drawTexture(currentImage.get(), Color.WHITE);
            if (ignoreAspectRatio) {
                ScaleMode scaleMode = canvas.getCurrentStyle().getTextureScaleMode();
                canvas.getCurrentStyle().setTextureScaleMode(ScaleMode.STRETCH);
                canvas.drawTexture(currentImage.get(), Color.WHITE);
                canvas.getCurrentStyle().setTextureScaleMode(scaleMode);
            } else {
                canvas.drawTexture(currentImage.get(), Color.WHITE);
            }
        }
    }

    @Override
    public void update(float delta) {

        if (isActive() && timestamp + speed * 1000 < new Date().getTime()) {
            timestamp = new Date().getTime();
            nextImage();
        }

        super.update(delta);
    }

    private void nextImage() {
        int size = images.size();
        int prevIndex = index;
        if (size > 0) {
            if (index == size - 1) {
                if (infinite) {
                    index = 0;
                } else {
                    stop();
                }
            } else {
                index++;
            }
            if (prevIndex != index) {
                currentImage.set(images.get(index));
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (currentImage.get() != null) {
            return currentImage.get().size();
        }
        return Vector2i.zero();
    }

    public void addImage(final TextureRegion textureRegion) {
        images.add(textureRegion);
        if (currentImage.get() == null) {
            currentImage.set(images.get(index));
        }
    }

    public void clean() {
        index = 0;
        currentImage.set(null);
        images = new ArrayList<>();
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        active = true;
    }

    public void stop() {
        active = false;
    }

}
