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

import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * This widget displays images in sequence in an image slideshow.
 * Switching images automatically after a time period or manually.
 */
public class UIImageSlideshow extends CoreWidget {

    private boolean active = true;
    private int index = 0;
    private float imageDisplayTime = 0f;

    /**
     * List of images to show.
     */
    @LayoutConfig
    private List<UIImage> images = new ArrayList<>();

    /**
     * Speed of slideshow (in seconds).
     */
    @LayoutConfig
    private float speed = 5f;

    /**
     * Whether the slideshow infinite.
     */
    @LayoutConfig
    private boolean infinite = true;

    /**
     * Whether the slideshow automatically switch images.
     */
    @LayoutConfig
    private boolean auto = true;

    @Override
    public void onDraw(Canvas canvas) {
        if (isImageExists()) {
            images.get(index).onDraw(canvas);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (isImageExists()) {
            return images.get(index).getPreferredContentSize(canvas, sizeHint);
        }
        return Vector2i.zero();
    }

    @Override
    public void update(float delta) {
        if (auto && active) {
            imageDisplayTime += delta;
            if (imageDisplayTime >= speed) {
                imageDisplayTime = 0f;
                nextImage();
            }
        }
        super.update(delta);
    }

    /**
     * Adds image to slideshow list.
     *
     * @param image the image to show.
     */
    public void addImage(final UIImage image) {
        if (image != null) {
            images.add(image);
        }
    }

    /**
     * Adds texture region to slideshow list.
     *
     * @param textureRegion the textureRegion to show.
     */
    public void addImage(final TextureRegion textureRegion) {
        addImage(new UIImage(textureRegion));
    }

    /**
     * Removes all images from slideshow list.
     */
    public void clean() {
        index = 0;
        imageDisplayTime = 0f;
        images = new ArrayList<>();
    }

    /**
     * Starts automatic slideshow.
     */
    public void start() {
        active = true;
    }

    /**
     * Stops automatic slideshow.
     */
    public void stop() {
        active = false;
    }

    /**
     * Checks active status of slideshow.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Switches to next image of list.
     */
    public void nextImage() {
        int size = images.size();
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
        }
    }

    /**
     * Switches to previous image of list.
     */
    public void prevImage() {
        int size = images.size();
        if (size > 0) {
            if (index == 0) {
                if (infinite) {
                    index = size - 1;
                } else {
                    stop();
                }
            } else {
                index--;
            }
        }
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public List<UIImage> getImages() {
        return images;
    }

    private boolean isImageExists() {
        return !images.isEmpty() && index < images.size() && images.get(index) != null;
    }
}
