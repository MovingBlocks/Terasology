// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components;

import org.joml.Vector2f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.rendering.assets.texture.Texture;

/**
 *
 */
@API
public class ParticleDataSpriteComponent implements Component {
    /**
     * This system's particle texture
     */
    public Texture texture;

    /**
     * This system's particle texture size, in percents x: [0.0, 1.0], y: [0.0, 1.0]
     */
    public Vector2f textureSize = new Vector2f(1.0f, 1.0f);
}
