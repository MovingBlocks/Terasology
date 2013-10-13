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

/**
 * @author Immortius
 */
public enum ScaleMode {
    /**
     * Stretches to fill the given space
     */
    STRETCH,
    /**
     * Scales to fill the given space. Parts of the image will be cut off if it is a different shape to the space.
     */
    SCALE_FILL,
    /**
     * Scales to fit in the given space. There will be gaps if the image is a different shape to the space.
     */
    SCALE_FIT,

    /**
     * Repeats the image to fill the given space
     */
    REPEAT
}
