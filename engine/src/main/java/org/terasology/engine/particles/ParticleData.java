// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.terasology.context.annotation.API;

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
