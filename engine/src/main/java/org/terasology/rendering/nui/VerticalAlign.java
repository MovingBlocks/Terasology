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

import org.terasology.math.Rect2i;

/**
 * @author Immortius
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

    public abstract int getOffset(int elementHeight, int availableHeight);

    public int getStart(Rect2i region) {
        return region.minY() + getOffset(0, region.height());
    }
}
