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
package org.terasology.rendering.particles;

import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.particles.internal.DataMask;
import org.terasology.rendering.particles.internal.ParticlePool;

/**
 * Data object to store the data of a single particle.
 * Used internally for swapping operations.
 */
public final class ParticleData {
    // scalars
    public float size;
    public float energy;

    // 3d vectors
    public final Vector3f position = new Vector3f();
    public final Vector3f previousPosition = new Vector3f();
    public final Vector3f velocity = new Vector3f();

    // 4d vectors
    public final Vector4f color = new Vector4f();
}
