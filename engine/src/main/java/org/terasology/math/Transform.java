/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.math;

import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

public class Transform {
    public final Vector3f origin;
    public final Quat4f rotation;
    public float scale;

    public Transform(Vector3f origin, Quat4f rotation, float scale) {
        this.origin = origin;
        this.rotation = rotation;
        this.scale = scale;
    }
}
