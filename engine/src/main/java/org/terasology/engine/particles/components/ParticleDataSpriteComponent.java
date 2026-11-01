// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components;

import org.joml.Vector2f;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;


@API
public class ParticleDataSpriteComponent implements Component<ParticleDataSpriteComponent> {
    /**
     * This system's particle texture
     */
    public Texture texture;

    /**
     * This system's particle texture size, in percents x: [0.0, 1.0], y: [0.0, 1.0]
     */
    public Vector2f textureSize = new Vector2f(1.0f, 1.0f);

    @Override
    public void copyFrom(ParticleDataSpriteComponent other) {
        this.texture = other.texture;
        this.textureSize = new Vector2f(other.textureSize);
    }
}
