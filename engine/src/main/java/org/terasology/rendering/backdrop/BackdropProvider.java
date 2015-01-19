/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.backdrop;

import org.terasology.math.geom.Vector3f;

/**
 * Implementations of this interface provide read and/or write access to the backdrop,
 * the visual layer that is rendered beyond the 3d scene rendered by the WorldRenderer
 *
 * This is generally intended to be the sky, but the theatrical term "backdrop" is used to include
 * anything in the background. On the other hand, the term "background" itself was avoided as it is
 * used in the context of background/foreground processes and threads.
 *
 * Created by manu on 13.01.2015.
 */
public interface BackdropProvider {

    // Note: I intentionally leave this interface undocumented (which is how I found it
    // in the skysphere implementation) as I plan to radically change it. -- emanuele3d

    float getDaylight();

    float getTurbidity();

    float getColorExp();

    float getSunPositionAngle();

    Vector3f getQuantizedSunDirection(float stepSize);

    Vector3f getSunDirection(boolean moonlightFlip);

}
