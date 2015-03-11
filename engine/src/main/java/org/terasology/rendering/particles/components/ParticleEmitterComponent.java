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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Linus on 1-3-2015.
 */
public class ParticleEmitterComponent implements Component {
    public static final int INFINITE_PARTICLE_SPAWNS = -1;

    public float spawnRateMax = 11.0f;
    public float spawnRateMin = 9.0f;

    public boolean isEmmiting = true;

    public float maxLifeTime = Float.POSITIVE_INFINITY;
    public int particleSpawnsLeft = INFINITE_PARTICLE_SPAWNS;

    @Owns
    public List<EntityRef> generators = new ArrayList<>();
}
