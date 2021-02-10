/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.particles;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.terasology.module.sandbox.API;

/**
 * Data object to store the data of a single particle.
 * Used for generator and affector operations.
 */

@API
public final class ParticleData {
    // scalars
    public float energy;

    // 2d vectors
    public final Vector2f textureOffset = new Vector2f();

    // 3d vectors
    public final Vector3f position = new Vector3f();
    public final Vector3f previousPosition = new Vector3f();
    public final Vector3f velocity = new Vector3f();
    public final Vector3f scale = new Vector3f();

    // 4d vectors
    public final Vector4f color = new Vector4f();
}
