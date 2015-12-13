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
package org.terasology.rendering.nui;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;

/**
 * Describes the possible methods drawing to a region of a different size to the image being drawn.
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

        @Override
        public String toString() {
            return "stretch";
        }
    },

    /**
     * Scales to fit in the given space. There will be gaps if the image is a different shape (aspect) to the space.
     */
    SCALE_FIT {
        @Override
        public Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight) {
            float scale = Math.min((float) region.width() / actualWidth, (float) region.height() / actualHeight);
            return new Vector2f(actualWidth * scale, actualHeight * scale);
        }

        @Override
        public String toString() {
            return "scale fit";
        }
    },

    /**
     * Scales to fill the given space. Parts of the image will be cut off if it is a different shape to the space.
     */
    SCALE_FILL {
        @Override
        public Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight) {
            float scale = Math.max((float) region.width() / actualWidth, (float) region.height() / actualHeight);
            return new Vector2f(actualWidth * scale, actualHeight * scale);
        }

        @Override
        public String toString() {
            return "scale fill";
        }
    },

    /**
     * Tiles the texture to fill the given space.
     */
    TILED {
        @Override
        public Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight) {
            return new Vector2f(region.width(), region.height());
        }

        @Override
        public String toString() {
            return "tiled";
        }
    };

    /**
     * Provides the final size to draw the  when drawing with this ScaleMode to the given region.
     * @param region
     * @param actualWidth
     * @param actualHeight
     * @return The relative scale to draw the
     */
    public abstract Vector2f scaleForRegion(Rect2i region, int actualWidth, int actualHeight);
}
