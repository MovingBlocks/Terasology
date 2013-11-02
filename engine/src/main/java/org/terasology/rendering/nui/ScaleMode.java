/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui;

import org.terasology.math.Rect2i;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public enum ScaleMode {
    /**
     * Stretches to fill the given space
     */
    STRETCH {
        @Override
        public Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight) {
            return new Vector2f(region.width(), region.height());
        }
    },

    /**
     * Scales to fit in the given space. There will be gaps if the image is a different shape (aspect) to the space.
     */
    SCALE_FIT {
        @Override
        public Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight) {
            Vector2f scale = new Vector2f();
            float aspect = ((float) actualWidth) / actualHeight;
            if (aspect > 0) {
                scale.x = (float) Math.ceil(region.height() * aspect);
                scale.y = region.height();
            } else {
                scale.x = region.width();
                scale.y = (float) Math.ceil(region.width() / aspect);
            }
            return scale;
        }
    },

    /**
     * Scales to fill the given space. Parts of the image will be cut off if it is a different shape to the space.
     */
    SCALE_FILL {
        @Override
        public Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight) {
            Vector2f scale = new Vector2f();
            float aspect = ((float) actualWidth) / actualHeight;
            if (aspect > 0) {
                scale.x = region.width();
                scale.y = (float) Math.ceil(region.width() / aspect);
            } else {
                scale.x = (float) Math.ceil(region.height() * aspect);
                scale.y = region.height();
            }
            return scale;
        }
    },

    /**
     * Tiles the texture to fill the given space.
     */
    TILED {
        @Override
        public Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight) {
            return new Vector2f(1, 1);
        }
    };

    public abstract Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight);
}
