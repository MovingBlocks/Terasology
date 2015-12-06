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
 * Possible vertical alignments. Provides support for determining where to position something vertically, given its size and the size of the space.
 */
public enum VerticalAlign {
    TOP {
        @Override
        public int getOffset(int elementHeight, int availableHeight) {
            return 0;
        }
    },
    MIDDLE {
        @Override
        public int getOffset(int elementHeight, int availableHeight) {
            return (availableHeight - elementHeight) / 2;
        }
    },

    BOTTOM {
        @Override
        public int getOffset(int elementHeight, int availableHeight) {
            return availableHeight - elementHeight;
        }
    };

    /**
     * @param elementHeight The height of the element to align
     * @param availableHeight The space in which to place it
     * @return The offset for the top of the element.
     */
    public abstract int getOffset(int elementHeight, int availableHeight);

    /**
     * Provides the "start" edge for the alignment - so for TOP it is the top edge, for BOTTOM it is the bottom edge
     * @param region
     * @return Where placement in the region begins for the alignment.
     */
    public int getStart(Rect2i region) {
        return region.minY() + getOffset(0, region.height());
    }
}
