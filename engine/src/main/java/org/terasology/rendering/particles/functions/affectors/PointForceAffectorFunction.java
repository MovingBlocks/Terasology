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
package org.terasology.rendering.particles.functions.affectors;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.rendering.particles.components.affectors.PointForceAffectorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 13-4-2015.
 */
public class PointForceAffectorFunction extends AffectorFunction<PointForceAffectorComponent> {
    public PointForceAffectorFunction() {
        super(PointForceAffectorComponent.class, DataMask.VELOCITY, DataMask.POSITION);
    }

    @Override
    public void update(PointForceAffectorComponent component, ParticleData particleData, Random random, float delta) {
        Vector3f vector = new Vector3f(particleData.position);
        vector.sub(component.position);

        float t = TeraMath.clamp(vector.length(), 0.0f, component.radius) / component.radius;
        float force = TeraMath.lerp(component.magnitude, 0.0f, t);
        vector.normalize().scale(force * delta);

        particleData.velocity.add(vector);
    }
}
