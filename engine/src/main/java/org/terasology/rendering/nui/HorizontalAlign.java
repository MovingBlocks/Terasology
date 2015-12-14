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

/**
 */
public enum HorizontalAlign {
    /**
     * Starts on the left edge of the area
     */
    LEFT {
        @Override
        public int getOffset(int elementWidth, int availableWidth) {
            return 0;
        }
    },
    /**
     * Ends on the right edge of the area
     */
    RIGHT {
        @Override
        public int getOffset(int elementWidth, int availableWidth) {
            return availableWidth - elementWidth;
        }
    },
    /**
     * Centered in the middle of the area, with equal space on each side.
     */
    CENTER {
        @Override
        public int getOffset(int elementWidth, int availableWidth) {
            return (availableWidth - elementWidth) / 2;
        }
    };

    /**
     * Given the elementWidth and availableWidth, the offset of the element so that it will be correctly aligned
     *
     * @param elementWidth   The width of the element being drawn
     * @param availableWidth The width of the available space for the element
     * @return The horizontal offset that is needed to align the element
     */
    public abstract int getOffset(int elementWidth, int availableWidth);

    /**
     * Given a region, gets the start position for the alignment
     *
     * @param region
     * @return
     */
    public int getStart(Rect2i region) {
        return region.minX() + getOffset(0, region.width());
    }

}
