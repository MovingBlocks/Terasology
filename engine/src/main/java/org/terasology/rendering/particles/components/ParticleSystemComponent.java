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
package org.terasology.rendering.particles.components;

import com.google.common.collect.Lists;
import org.lwjgl.util.vector.Vector2f;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.assets.texture.Texture;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Linus van Elswijk
 */
public class ParticleSystemComponent implements Component {

    public float maxLifeTime = Float.POSITIVE_INFINITY;
    public boolean destroyWhenFinished = true;

    public int nrOfParticles = 1000;

    @Owns
    public EntityRef emitter = null;

    @Owns
    public List<EntityRef> affectors = new ArrayList<>();

    public Texture texture = null;
    public Vector2f textureSize = new Vector2f(1.0f, 1.0f);
}
