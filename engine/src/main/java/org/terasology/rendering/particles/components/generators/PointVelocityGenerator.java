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
package org.terasology.rendering.particles.components.generators;

import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 9-3-2015.
 */
public class PointVelocityGenerator implements Generator {
    public Vector3f point = new Vector3f();
    public float magnitude = 1.0f;

    public PointVelocityGenerator(Vector3f point, float magnitude) {
        this.point.set(point);
        this.magnitude = magnitude;
    }

    @Override
    public void onEmission(ParticleData particleData, Random random) {
        particleData.velocity.set((new Vector3f(particleData.position)).sub(point).normalize().scale(magnitude));
    }
}
