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
package org.terasology.engine.particles.components;

import org.joml.Vector2f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.module.sandbox.API;
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
